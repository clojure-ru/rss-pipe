(ns rss-pipe.web
  (:require [mount.core :as mount]
            [debugger.core :as debugger]
            [ring.adapter.undertow :as undertow]
            [rss-pipe.config :as config]))

(defn app
  [req]
  {:status 200
   :headers {"Content-Type" "application/atom+xml; charset=utf-8"}
   :body (-> config/config :github-rss slurp debugger/dbg)})

(defn create-web
  []
  (undertow/run-undertow #'app {:port (-> config/config :wev :port (or 4000))}))

(defn safe-stop-web
  [server]
  (if server (.stop server)))

(mount/defstate web
  :start (create-web)
  :stop (safe-stop-web web))
