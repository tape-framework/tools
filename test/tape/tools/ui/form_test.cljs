(ns tape.tools.ui.form-test
  (:require [cljs.test :refer [deftest testing is are]]
            [integrant.core :as ig]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rft]
            [tape.module :as module :include-macros true]
            [tape.mvc :as mvc :include-macros true]
            [tape.tools.ui.form :as form :include-macros true]
            [tape.tools.app.controller :as app.c]))

(module/load-hierarchy)

;;; Lens

(def ^:private config
  {::mvc/module nil
   ::app.c/module nil})

(deftest lens-test
  (let [system (-> config module/prep-config ig/init)]
    (testing "lens*"
      (rft/run-test-sync
       (let [lens (form/lens* ::app.c/todo ::app.c/field)
             cursor (r/cursor lens [:done])]
         (reset! cursor false)
         (is (false? @cursor))
         (swap! cursor not)
         (is (true? @cursor)))))
    (testing "lens"
      (rft/run-test-sync
       (let [lens (form/lens app.c/todo app.c/field)
             cursor (r/cursor lens [:done])]
         (reset! cursor false)
         (is (false? @cursor))
         (swap! cursor not)
         (is (true? @cursor)))))
    (ig/halt! system)))

;;; Input tag

(def ^:private options {:man "Man", :woman "Woman"})

(def ^:private options-tags
  '([:option {:key :man, :value :man} "Man"]
    [:option {:key :woman, :value :woman} "Woman"]))

(deftest input-tag-test
  (are [x y] (= (form/input-tag {:type x}) y)
    :text [:input {:type :text}]
    :textarea [:textarea {}]
    :checkbox [:input {:type :checkbox}]
    :radio [:input {:type :radio}]
    :select [:select {} nil ()])

  (are [x y] (= (form/input-tag (merge {:type :select} x)) y)

    ;; Single
    {:options options} [:select {} nil options-tags]
    {:value "man", :options options} [:select {:value "man"} nil options-tags]

    {:value "man", :options options, :include-blank true}
    [:select {:value "man"} [:option] options-tags]

    ;; Multiple
    {:multiple true, :options options}
    [:select {:multiple true} nil options-tags]

    {:multiple true, :value '("man"), :options options}
    [:select {:multiple true} nil
     '([:option {:key :man, :value :man, :selected true} "Man"]
       [:option {:key :woman, :value :woman} "Woman"])]

    {:multiple true, :value '("" "man"), :options options, :include-blank true}
    [:select {:multiple true} [:option {:value "", :selected true}]
     '([:option {:key :man, :value :man, :selected true} "Man"]
       [:option {:key :woman, :value :woman} "Woman"])]))

;;; Input

(defn- is-input [typ expected]
  (let [post      (r/atom {:title "title"})
        actual    (form/field {:type typ, :source post, :field :title})
        [actual-tag actual-attrs] actual
        on-change (:on-change actual-attrs)
        [expected-tag expected-attrs] expected]
    (is (= expected-tag actual-tag))
    (when-not (= :textarea actual-tag)
      (is (= (:type expected-attrs) (:type actual-attrs))))
    (is (= (:value expected-attrs) (:value actual-attrs)))
    (on-change (clj->js {:target {:value "title2"}}))
    (is (= "title2" (:title @post)))))

(defn- is-tick [typ visibility expected]
  (let [post      (r/atom {:visibility visibility})
        invert    {"public" nil, nil "public"}
        actual    (form/field {:type   typ
                               :source post
                               :field  :visibility
                               :value  "public"})
        [actual-tag actual-attrs] actual
        on-change (:on-change actual-attrs)
        [expected-tag expected-attrs] expected]
    (is (= expected-tag actual-tag))
    (are [k] (= (k expected-attrs) (k actual-attrs)) :type :checked)
    (on-change #js {})
    (is (= (invert visibility) (:visibility @post)))))

(defn- is-select [m expected]
  (let [post      (r/atom {:gender "man"})
        defaults  {:type    :select
                   :options options
                   :source  post
                   :field   :gender}
        actual    (form/field (merge defaults m))
        [actual-tag actual-attrs & actual-children] actual
        on-change (:on-change actual-attrs)
        [expected-tag expected-attrs & expected-children] expected]
    (is (= expected-tag actual-tag))
    (are [k] (= (k expected-attrs) (k actual-attrs)) :type :value)
    (is (= expected-children actual-children))
    (on-change (clj->js {:target {:value "woman"}}))
    (is (= "woman" (:gender @post)))))

(defn- is-select-multiple
  [m initial-value event-options expected-hiccup expected-value]
  (let [post      (r/atom {:gender initial-value})
        defaults  {:type    :select
                   :options options
                   :source  post
                   :field   :gender}
        actual    (form/field (merge defaults m))
        [actual-tag actual-attrs & actual-children] actual
        on-change (:on-change actual-attrs)
        [expected-tag expected-attrs & expected-children] expected-hiccup]
    (is (= expected-tag actual-tag))
    (is (= (:type expected-attrs) (:type actual-attrs)))
    (is (= expected-children actual-children))
    (on-change (clj->js {:target {:options event-options}}))
    (is (= expected-value (:gender @post)))))

(deftest input-test
  (is-input :text [:input {:type :text, :value "title"}])
  (is-input :textarea [:textarea {:type :textarea, :value "title"}])

  (is-tick :checkbox nil
           [:input {:type :checkbox, :value "public", :checked false}])
  (is-tick :checkbox "public"
           [:input {:type :checkbox, :value "public", :checked true}])

  (is-tick :radio nil [:input {:type :radio, :value "public", :checked false}])
  (is-tick :radio "public"
           [:input {:type :radio, :value "public", :checked true}])

  (is-select {} [:select {:value "man"} nil options-tags])
  (is-select {:include-blank true}
             [:select {:value "man"} [:option] options-tags])

  (is-select-multiple
   {:multiple true}
   '("man")
   [{:value "man", :selected true}, {:value "woman", :selected true}]
   [:select {:multiple true} nil
    '([:option {:key :man, :value :man, :selected true} "Man"]
      [:option {:key :woman, :value :woman} "Woman"])]
   '("man" "woman"))

  (is-select-multiple
   {:multiple true, :include-blank true}
   '("" "man")
   [{:value "man", :selected true}, {:value "woman", :selected true}]
   [:select {:multiple true} [:option {:value "", :selected true}]
    '([:option {:key :man, :value :man, :selected true} "Man"]
      [:option {:key :woman, :value :woman} "Woman"])]
   '("man" "woman")))

;; Field with errors

(deftest field-with-list-test
  (let [output (form/field-with-errors {:type   :text, :class "input",
                                        :source (r/atom {:field nil}), :field :field
                                        :errors #{"Must be present" "Must be numeric"}})]
    (is (= "input is-danger"
           (-> output second :class)))))

(deftest field-with-list-errors-test
  (let [output (form/field-with-list-errors {:type   :text, :class "input",
                                             :source (r/atom {:field nil}), :field :field
                                             :errors #{"Must be present" "Must be numeric"}})]
    (is (= '([:p.help.is-danger {:key "Must be present"} "Must be present"]
             [:p.help.is-danger {:key "Must be numeric"} "Must be numeric"])
           (last output)))))

;;; When valid

(deftest when-valid-test
  (let [cb (form/when-valid (constantly 42))
        mk (fn [valid]
             (clj->js {:preventDefault (fn [])
                       :target         {:form {:checkValidity  (fn [] valid)
                                               :reportValidity (fn [] 57)}}}))]
    (is (= 42 (cb (mk true))))
    (is (= 57 (cb (mk false))))))
