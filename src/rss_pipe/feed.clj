(ns rss-pipe.feed
  (:require [clojure.zip :as zip]
            [rss-pipe.github.repo :as repo]
            [rss-pipe.config :as config]
            [rss-pipe.cache :as cache]
            [clojure.xml :as xml]))


(defn loc->str [loc] (with-out-str (xml/emit-element (zip/node loc))))

(defn process-xml [tree]
  (loop [loc tree buf {} acc []]
    (cond
      (zip/end? loc)
      {:acc acc :xml (zip/root loc)}

      :else
      (case (-> loc zip/node :tag)
        :title
        (recur (zip/next loc) (assoc buf :title (-> loc zip/node :content first)) acc)

        :link
        (recur (zip/next loc) (assoc buf :link (-> loc zip/node :attrs :href)) acc)

        :content
        (recur (zip/next (zip/remove loc)) (assoc buf :content-node (zip/node loc)) acc)

        :entry
        (let [{:keys [link content-node title]} buf
              content* (when (and content-node
                                       link (repo/repo-link? link)
                                       title #_(re-find #"starred" title))
                         ;; (str "!! " link)
                         (str (repo/entry-readme-html link))
                         )]
          (recur
           (if content*
             (zip/next (zip/edit loc #(update-in % [:content] conj {:tag :!!! :content [content*] :attrs {:x 1}})))
             (zip/next loc)
             )
           ;; (zip/next (zip/replace loc (zip/node loc*)))
           ;; (zip/next (zip/edit loc #(update-in % [:content] conj (pr-str content-node*))))
           {} (if content*
                (conj acc (assoc buf
                                 ;; :entry (loc->str loc)
                                 :content-node nil))
                acc))) ; start & flush prev entry findings

        (recur (zip/next loc) buf acc) ; else
        ))))

(defn new-feed []
  (-> config/config
      :github-rss
      xml/parse
      zip/xml-zip
      process-xml))

(defn xml-feed []
  (-> (new-feed)
      :xml
      xml/emit-element
      with-out-str))
