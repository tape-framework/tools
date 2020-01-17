(ns tape.tools.intervals.controller
  (:refer-clojure :exclude [set])
  (:require [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]))

;;; Effects

(defn ^{::c/fx ::set} set-fx [m]
  (let [{:keys [ms set interval]} m
        id (js/setInterval #(rf/dispatch interval) ms)]
    (rf/dispatch (conj set id))))

(defn ^{::c/fx ::clear} clear-fx [id] (js/clearInterval id))

;;; Events

(defn ^::c/event-fx set [_ [_ m]] {::set m})
(defn ^::c/event-fx clear [_ [_ id]] {::clear id})

;;; Module

(c/defmodule)
