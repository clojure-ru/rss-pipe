(ns rss-pipe.github.repo
  (:require [byte-transforms :refer [decode]]
            [clj-http.client :as http]
            [rss-pipe.cache :as cache]
            [cheshire.core :as json]
            [markdown.core :as md]
            [clojure.string :as string]))

(defn- link->api-url [link]
  (string/replace link "https://github.com/" "https://api.github.com/repos/")
  ;; (string/replace link "https://github.com/" "http://127.0.0.1:8081/")
  )

(defn repo-link? [link]
  (re-find #"https://github.com/[^\/]+/[^\/]+$" link))

(defn github-repo [url]
  (cache/fetch url #(http/get % {:throw-exceptions false})))

(defn entry-readme-html [link]
  (some-> link
          link->api-url
          (str "/readme")
          github-repo
          ;; (http/get {:debug true})
          :body
          ;; cache/slurp
          (json/parse-string true)
          :content
          (decode :base64)
          (String.)
          md/md-to-html-string
          ))
