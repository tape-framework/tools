(ns tape.tools.app.controller
  (:require [tape.mvc.controller :as c :include-macros true]))

(defn sub
  {::c/reg ::c/sub}
  [db _query]
  (::x db))

(defn event-db
  {::c/reg ::c/event-db}
  [_db [_ev-id _params]]
  {::x "x"})

(c/defmodule)
