(ns rss-pipe.github.repo
  (:require [byte-transforms :refer [decode]]
            [clj-http.client :as http]
            [rss-pipe.cache :as cache]
            [cheshire.core :as json]
            [markdown.core :as md]
            [rss-pipe.config :as config]
            [taoensso.timbre :as timbre]
            [clojure.string :as string]))

(defn link->api-url [link]
  (string/replace link "https://github.com/" "https://api.github.com/repos/"))

(defn repo-link? [link]
  (re-find #"https://github.com/[^\/]+/[^\/]+$" link))

(defn github-repo [url]
  (try
    (cache/fetch url #(http/get % {:basic-auth (:github-http-auth config/config)}))
    (catch Exception e
      ;; log is streaming to response: 404 errors
      #_(timbre/error (str "Error fetching " url " :" e))
      nil)))

(defn entry-readme-html [link]
  (some-> link
          link->api-url
          (str "/readme")
          github-repo
          :body
          (json/parse-string true)
          :content
          (decode :base64)
          (String.)
          md/md-to-html-string))
