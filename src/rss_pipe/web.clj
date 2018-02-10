(ns rss-pipe.web
  (:require [mount.core :as mount]
            [debugger.core :as debugger]
            [ring.adapter.undertow :as undertow]
            [ring.adapter.jetty :as jetty]
            [ring.logger :as logger]
            [rss-pipe.config :as config]
            [rss-pipe.cache :as cache]
            [rss-pipe.feed :as feed]))

(defn cached-feed []
  (cache/fetch :feed (fn [_] (feed/xml-feed))))

(defn handler
  [req]
  {:status 200
   :headers {"Content-Type" "application/atom+xml; charset=utf-8"}
   :body (cached-feed)})

(def app (logger/wrap-with-logger handler))

(defn create-web
  []
  (let [port (-> config/config :port (or 4000))]
    #_(undertow/run-undertow #'app {:port port})
    (jetty/run-jetty #'app {:port port :join? false})))

(defn safe-stop-web
  [server]
  (if server (.stop server)))

(mount/defstate web
  :start (create-web)
  :stop (safe-stop-web web))
