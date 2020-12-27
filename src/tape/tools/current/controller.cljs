(ns tape.tools.current.controller
  (:require [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]
            [tape.mvc.view :as v]))

;;; Subs

(defn view
  {::c/reg ::c/sub}
  [db _] (::view db))

;;; Integrant

;; Re-Frame subscription yielding the Reagent function set as `::view` in
;; app-db.
(defmethod ig/init-key ::view-fn [_ views]
  (fn [db _] (get views (view db nil))))

;; An interceptor that is added in the middleware stack of Tape Re-Frame
;; event handlers that have a view with the same name in the corresponding
;; views namespace; this interceptor sets the `::view` view automatically
;; if it's not already present in app-db.
(defmethod ig/init-key ::view-interceptor [_ views]
  (rf/enrich (fn add-view [db event]
               (let [event-id (first event)
                     view (::view db)
                     add? (and (nil? view) (contains? views event-id))]
                 (cond-> db
                         add? (assoc ::view event-id))))))

;;; Module

(c/defmodule {::view-interceptor (ig/ref ::v/views)
              ::view-fn (ig/ref ::v/views)})
