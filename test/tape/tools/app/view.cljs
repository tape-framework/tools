(ns tape.tools.app.view
  (:require [tape.mvc.view :as v :include-macros true]
            [tape.tools.app.controller]))

;;; Views

(defn event-db
  {::v/reg ::v/view}
  [] [:p "p"])

;;; Module

(v/defmodule)
