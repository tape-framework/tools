(ns tape.tools.intervals.controller-test
  (:refer-clojure :exclude [set])
  (:require [cljs.test :refer [deftest testing is]]
            [day8.re-frame.test :as rft]
            [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.mvc :as mvc :include-macros true]
            [tape.module :as module :include-macros true]
            [tape.tools.intervals.controller :as intervals.c]
            [tape.tools.app.controller :as app.c]))

(module/load-hierarchy)

(def ^:private config
  {::mvc/module nil
   ::intervals.c/module nil})

(deftest interval-test
  (let [system (-> config module/prep-config ig/init)]
    (testing "set"
      (rft/run-test-async
       (let [interval {:ms 1
                       :set [::app.c/set-interval]
                       :interval [::app.c/interval]}
             interval-id (rf/subscribe [::app.c/interval-id])
             interval-count (rf/subscribe [::app.c/interval-count])]
         (rf/dispatch [::intervals.c/set interval])
         (rft/wait-for [::app.c/interval]
           (is (integer? @interval-id))
           (is (= 1 @interval-count))
           (rft/wait-for [::app.c/interval]
             (is (= 2 @interval-count)))))))

    (testing "clear"
      (rft/run-test-sync
       (let [interval {:ms 1
                       :set [::intervals.c/clear]
                       :interval [::does-not-dispatch]}
             id (atom nil)]
         (with-redefs [js/setInterval (constantly 42)
                       js/clearInterval #(reset! id %)]
           (rf/dispatch [::intervals.c/set interval])
           (is (= 42 @id))))))
    (ig/halt! system)))
