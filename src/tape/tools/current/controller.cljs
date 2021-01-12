(ns tape.tools.current.controller
  (:require [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.mvc :as mvc :include-macros true]))

;;; Subs

(defn view
  {::mvc/reg ::mvc/sub}
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
(prefer-method ig/init-key ::view-fn :tape/const)
(mvc/defm ::module
          {::view-interceptor (ig/ref ::mvc/views)
           [::view-fn ::mvc/sub] (ig/ref ::mvc/views)})
