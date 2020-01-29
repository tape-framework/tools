## README

STATUS: Pre-alpha, in design and prototyping phase.

### About

`tape.tools`

Some common pieces of infrastructure:
- input data helpers
- timeouts & intervals effects

### Install

Add `tape.tools` to your deps:

```clojure
tape/tools {:local/root "../tools"}
```

### Usage

You must be familiar with `tape.module` and `tape.mvc` (particularly the
controller part) before proceeding.

#### The lens pattern

In your view namespace require `tape.tools`:

```clojure
(ns blog.app.posts.view
  (:require [reagent.core :as r]
            [tape.tools :as tools]))
```

A `tools/lens` is a helper for making cursors that read from a subscription and
write by dispatching an event.

```clojure
(defn form-fields []
  (let [lens (tools/lens ::posts.c/post ::posts.c/field)
        title (r/cursor lens [:title])]
    [:input {:value @title
             :on-change #(reset! title (-> % .-target .-value))}]))
```

When the `title` cursor above is `reset!`, it dispatches an event:
`[::posts.c/field :title "some value"]`. When it is `deref`'ed it reads the:
`(:title @(rf/subscription [::posts.c/post]))` value.

`tools/cursor` directly returns a Reagent cursor over a lens.

#### Form tools

In your view namespace require `tape.tools.ui.form`:

```clojure
(ns blog.app.posts.view
  (:require [tape.tools.ui.form :as form]))
```

##### Inputs

The `form/field` helper creates an input over an atom field. It's usually used
in conjunction with a cursor over a `tools/lens`.

```clojure
(defn form-fields []
  (let [cursor (tools/cursor ::posts.c/post ::posts.c/field)]
    [:<>
     [form/field {:type :text, :state cursor, :field :title}]
     [form/field {:type :textarea, :state cursor, :field :description}]]))

```

##### Constraint Validation

The `form/when-valid` helper can be used on forms to to check and show the
HTML5 Constraint Validation API.

```clojure
(defn save-button []
  (let [save #(rf/dispatch [::posts.c/save])]
    [:button {:on-click (form/when-valid save)} "Save"]))
```

If the form to which the button above belongs is valid, on clicking the button
the `save` function is called; if the form is invalid the `save` function is
not called and the form validity is reported.

#### Timeouts and Intervals

We have two controllers with event handlers and effect handlers for setting and
clearing timeouts and intervals.

```clojure
(ns my.app
  (:require [tape.tools.timeouts.controller :as timeouts.c]
            [tape.tools.intervals.controller :as intervals.c]))
;; also add ::timeouts.c/module & ::intervals.c/module to the ig config map
```

A timeout is a map with 3 positions: the number of milliseconds, an event
that gets dispatched when the timeout is set (with the timeout id added) and
and event that gets dispatched when the timer times out:

```clojure
{:ms 3000
 :set [::was-set] ;; dispatched as [::was-set timeout-id]
 :timeout [::timed-out]}
```

Similarly, an interval:

```clojure
{:ms 3000
 :set [::was-set] ;; dispatched as [::was-set interval-id]
 :interval [::period-reached]}
```

These are given as arguments to events or effects, as follows:

```clojure
[::timeouts.c/set a-timeout] ;; event
[::timeouts.c/clear a-timeout-id] ;; event

{::timeouts.c/set a-timeout} ;; effect
{::timeouts.c/clear a-timeout-id} ;; effect

[::intervals.c/set an-interval] ;; event
[::intervals.c/clear an-interval-id] ;; event

{::intervals.c/set an-interval} ;; effect
{::intervals.c/clear an-interval-id} ;; effect
```

#### License

Copyright © 2019 clyfe

Distributed under the MIT license.
