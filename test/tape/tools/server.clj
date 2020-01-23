(ns tape.tools.server
  "A Ring handler for Figwheel to serve WebJars assets."
  (:require [ring.middleware.resource :as resource]
            [ring.util.response :as response]))

(defn handler [_req]
  (response/not-found "Not found"))

(def app
  (-> handler
      (resource/wrap-resource "/META-INF/resources/webjars")))
