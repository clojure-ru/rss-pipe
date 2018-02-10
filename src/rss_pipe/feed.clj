(ns rss-pipe.feed
  (:require [clojure.zip :as zip]
            [clj-time.core :as time]
            [clj-time.format :as timef]
            [rss-pipe.github.repo :as repo]
            [rss-pipe.config :as config]
            [rss-pipe.cache :as cache]
            [clojure.xml :as xml]))


(defn loc->str [loc] (with-out-str (xml/emit-element (zip/node loc))))

(defn process-xml [tree]
  (loop [loc tree buf nil acc []]
    (cond
      (or (zip/end? loc) (-> acc count (> 1)))
      acc

      :else
      (cond
        ;; :title
        ;; (recur (zip/next loc) (assoc buf :title (-> loc zip/node :content first)) acc)

        ;; :link
        ;; (recur (zip/next loc) (assoc buf :link (-> loc zip/node :attrs :href)) acc)

        ;; :content
        ;; ;; (recur (zip/next (zip/remove loc)) (assoc buf :content-node (zip/node loc)) acc)
        ;; (recur (zip/next loc) (assoc buf :content-node (zip/node loc)) acc)

        ;; :entry
        ;; (let [{:keys [link content-node title]} buf
        ;;       content* (when (and content-node
        ;;                                link (repo/repo-link? link)
        ;;                                title #_(re-find #"starred" title))
        ;;                  ;; (str "!! " link)
        ;;                  (str (repo/entry-readme-html link))
        ;;                  )]
        ;;   (recur
        ;;    (if content*
        ;;      (zip/next (zip/edit loc #(update-in % [:content] conj {:tag :content :content [content*] :attrs {:type "html"}})))
        ;;      ;; (zip/next (zip/edit loc #(update-in % [:content] conj {:tag :!!! :content [content*] :attrs {:x 1}})))
        ;;      (zip/next loc)
        ;;      )
        ;;    ;; (zip/next (zip/replace loc (zip/node loc*)))
        ;;    ;; (zip/next (zip/edit loc #(update-in % [:content] conj (pr-str content-node*))))
        ;;    {} (if content*
        ;;         (conj acc (assoc buf
        ;;                          ;; :entry (loc->str loc)
        ;;                          :content-node nil))
        ;;         acc))) ; start & flush prev entry findings
        (= :entry (-> loc zip/node :tag))
        (if (empty? buf)
          (recur (zip/next loc) {} acc)
          (recur (zip/next loc) {} (conj acc buf)))

        (and (some? buf) (-> loc zip/node :tag some?))
        (recur (zip/next loc) (assoc buf (-> loc zip/node :tag) (zip/node loc)) acc)

        :else
        (recur (zip/next loc) buf acc) ; else


        ))))

(defn link [entry] (-> entry :link :attrs :href))
(defn set-content [entry content] (assoc-in entry [:content :content 0] content))

(defn with-readme-content [entry]
  (let [readme (-> entry link repo/entry-readme-html)]
    (set-content entry readme)))

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
  (timef/unparse (timef/formatters :date-time) (time/now)))

(defn xml-feed-head []
  {:attrs {:xml:lang "en-US", :xmlns "http://www.w3.org/2005/Atom", :xmlns:media "http://search.yahoo.com/mrss/"},
   :content [{:attrs nil, :content ["tag:github.com,2008:/razum2um.private"], :tag :id}
             {:attrs {:href "https://github.com/razum2um", :rel "alternate", :type "text/html"}, :content nil, :tag :link}
             {:attrs nil, :content ["Feed for razum2um"], :tag :title} {:attrs nil, :content [(updated)], :tag :updated}], :tag :feed})

(defn xml-feed []
  (-> (xml-feed-head)
      (update :content concat (new-feed))
      xml/emit
      with-out-str))
