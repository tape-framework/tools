(ns tape.tools.app.controller
  (:refer-clojure :exclude [set])
  (:require [tape.mvc :as mvc :include-macros true]))

;;; Current view

(defn sub
  {::mvc/reg ::mvc/sub}
  [db _query]
  (::x db))

(defn event-db
  {::mvc/reg ::mvc/event-db}
  [_db [_ev-id _params]]
  {::x "x"})

;;; Lens

(defn field
  {::mvc/reg ::mvc/event-db}
  [db [_ k v]] (assoc-in db [::todo k] v))

(defn todo
  {::mvc/reg ::mvc/sub}
  [db _] (::todo db))

;;; Timeout

(defn set-timeout
  {::mvc/reg ::mvc/event-db}
  [db [_ id]] (assoc db ::timeout-id id))

(defn timeout
  {::mvc/reg ::mvc/event-db}
  [db [_]] (assoc db ::timeout-done true))

(defn timeout-id
  {::mvc/reg ::mvc/sub}
  [db _] (::timeout-id db))

(defn timeout-done
  {::mvc/reg ::mvc/sub}
  [db _] (::timeout-done db))

;;; Interval

(defn set-interval
  {::mvc/reg ::mvc/event-db}
  [db [_ id]]
  (assoc db ::interval-id id
            ::interval-count 0))

(defn interval
  {::mvc/reg ::mvc/event-db}
  [db _] (update db ::interval-count inc))

(defn interval-id
  {::mvc/reg ::mvc/sub}
  [db _] (::interval-id db))

(defn interval-count
  {::mvc/reg ::mvc/sub}
  [db _] (::interval-count db))

;;; Module

(mvc/defm ::module)
