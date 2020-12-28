(ns tape.tools.intervals.controller
  (:refer-clojure :exclude [set])
  (:require [re-frame.core :as rf]
            [tape.mvc :as mvc :include-macros true]))

;;; Effects

(defn set-fx
  {::mvc/reg ::mvc/fx
   ::mvc/id ::set}
  [ainterval]
  (let [{:keys [ms set interval]} ainterval
        interval-id (js/setInterval #(rf/dispatch interval) ms)]
    (when set
      (rf/dispatch (conj set interval-id)))))

(defn clear-fx
  {::mvc/reg ::mvc/fx
   ::mvc/id ::clear}
  [interval-id] (js/clearInterval interval-id))

;;; Events

(defn set
  {::mvc/reg ::mvc/event-fx}
  [_ [_ interval]] {::set interval})

(defn clear
  {::mvc/reg ::mvc/event-fx}
  [_ [_ interval-id]] {::clear interval-id})

;;; Module

(mvc/defm ::module)
