(ns rss-pipe.feed
  (:require [clojure.zip :as zip]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.format :as timef]
            [rss-pipe.github.repo :as repo]
            [rss-pipe.config :as config]
            [rss-pipe.cache :as cache]
            [clojure.xml :as xml]))

(defn loc->str [loc] (with-out-str (xml/emit-element (zip/node loc))))
(defn ok-tag? [tag]
  (and (some? tag) (not (#{:media:thumbnail} tag))))

(defn process-xml [tree]
  (loop [loc tree buf nil acc []]
    (cond
      (zip/end? loc)
      acc

      :else
      (cond
        (= :entry (-> loc zip/node :tag))
        (if (empty? buf)
          (recur (zip/next loc) {} acc)
          (recur (zip/next loc) {} (conj acc buf)))

        (and (some? buf) (-> loc zip/node :tag ok-tag?))
        (recur (or (zip/right loc) (zip/next loc)) (assoc buf (-> loc zip/node :tag) (zip/node loc)) acc)

        :else
        (recur (zip/next loc) buf acc)))))

(defn link [entry] (-> entry :link :attrs :href))
(defn set-content [entry content] (assoc-in entry [:content :content 0] content))

(defn escape-html [s]
  (string/escape s {\< "&lt;", \> "&gt;", \& "&amp;" \" "&quot;"}))

(defn with-readme-content [entry]
  (let [readme (some-> entry link repo/repo-link? repo/entry-readme-html escape-html)]
    (set-content entry (str readme))))

(defn new-entry [content]
  {:tag :entry :content content :attrs nil})

(defn new-feed []
  (->> config/config
       :github-rss
       xml/parse
       zip/xml-zip
       process-xml
       (pmap (comp new-entry vals with-readme-content))))

(defn updated []
  (.toString (.toInstant (java.util.Date.))))

(defn xml-feed-head []
  {:attrs {:xml:lang "en-US", :xmlns "http://www.w3.org/2005/Atom", :xmlns:media "http://search.yahoo.com/mrss/"},
   :content [{:attrs nil, :content ["tag:github.com,2008:/razum2um.private"], :tag :id}
             {:attrs {:href "https://github.com/razum2um", :rel "alternate", :type "text/html"}, :content nil, :tag :link}
             {:attrs {:href (:feed-self-link config/config), :rel "self", :type "application/atom+xml"}, :content nil, :tag :link}
             {:attrs nil, :content ["Feed for razum2um"], :tag :title} {:attrs nil, :content [(updated)], :tag :updated}], :tag :feed})

(defn emit-element [e]
  (if (instance? String e)
    (print e)
    (do
      (print (str "<" (name (:tag e))))
      (when (:attrs e)
        (doseq [attr (:attrs e)]
          (print (str " " (name (key attr)) "='" (val attr) "'"))))
      (if (:content e)
        (do
          (print ">")
          (doseq [c (:content e)]
            (emit-element c))
          (println (str "</" (name (:tag e)) ">")))
        (println "/>")))))

(defn emit [x]
  (println "<?xml version='1.0' encoding='UTF-8'?>")
  (emit-element x))

(defn xml-feed []
  (-> (xml-feed-head)
      (update :content concat (new-feed))
      emit
      with-out-str))
