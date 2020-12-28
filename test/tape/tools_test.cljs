(ns tape.tools-test
  (:require [cljs.test :refer [deftest testing is use-fixtures]]
            [integrant.core :as ig]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rft]
            [tape.mvc :as mvc :include-macros true]
            [tape.module :as module :include-macros true]
            [tape.tools :as tools :include-macros true]
            [tape.tools.app.controller :as app.c]))

(module/load-hierarchy)

;;; Ergonomic API

(deftest dispatch-test
  (with-redefs [rf/dispatch identity]
    (is (= [::app.c/event-db]
           (tools/dispatch [app.c/event-db])))))

(deftest subscribe-test
  (with-redefs [rf/subscribe identity]
    (is (= [::app.c/sub]
           (tools/subscribe [app.c/sub])))))

;;; Lens

(def ^:private config
  {::mvc/module nil
   ::app.c/module nil})

(deftest lens-test
  (let [system (-> config module/prep-config ig/init)]
    (testing "lens*"
      (rft/run-test-sync
       (let [lens (tools/lens* ::app.c/todo ::app.c/field)
             cursor (r/cursor lens [:done])]
         (reset! cursor false)
         (is (false? @cursor))
         (swap! cursor not)
         (is (true? @cursor)))))
    (testing "lens"
      (rft/run-test-sync
       (let [lens (tools/lens app.c/todo app.c/field)
             cursor (r/cursor lens [:done])]
         (reset! cursor false)
         (is (false? @cursor))
         (swap! cursor not)
         (is (true? @cursor)))))
    (ig/halt! system)))
