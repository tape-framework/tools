## README

STATUS: Pre-alpha, in design and prototyping phase.

#### About

`tape.tools`

Some common pieces of infrastructure:
- input data helpers
- timeouts & intervals effects

#### Usage

You must be familiar with `tape.module` and `tape.mvc` (particularly the
controller part) before proceeding.

##### Install

Add `tape.tools` to your deps:

```clojure
tape/tools {:local/root "../tools"}
```

##### Input data helpers

In your view namespace require `tape.tools`:

```cljs
(ns blog.app.posts.view
  (:require [reagent.core :as r]
            [tape.tools :as tools]))
```

**The lens pattern**

A `tools/lens` is a helper for making cursors that read from a subscription and
write by dispatching an event.

```cljs
(defn form-fields []
  (let [lens (tools/lens ::posts.c/post ::posts.c/field)
        title (r/cursor lens [:title])]
    [:input {:value @title
             :on-change #(reset! title (-> % .-target .-value))}]))
```

When the `title` cursor above is `reset!`, it dispatches an event:
`[::posts.c/field :title "some value"]`. When it is `deref`'ed it reads the:
`(:title @(rf/subscription [::posts.c/post]))` value.

**HTML5 Constraint Validation**

The `tools/when-valid` helper can be used on forms to to check and show the
HTML5 Constraint Validation API.

```cljs
(defn save-button []
  (let [save #(rf/dispatch [::posts.c/save])]
    [:button {:on-click (ui/when-valid save)} "Save"]))
```

If the form to which the button above belongs is valid, on clicking the button
the `save` function is called; if the form is invalid the `save` function is
not called and the form validity is reported.

##### Timeouts and Intervals

We have two controllers with event handlers and effect handlers for setting and
clearing timeouts and intervals.

```cljs
(ns my.app
  (:require [tape.tools.timeouts.controller :as timeouts.c]
            [tape.tools.intervals.controller :as intervals.c]))
;; also add ::timeouts.c/module & ::intervals.c/module to the ig config map
```

A timeout is a map with 3 positions: the number of milliseconds, an event
that gets dispatched when the timeout is set (with the timeout id added) and
and event that gets dispatched when the timer times out:

```cljs
{:ms 3000
 :set [::was-set] ;; dispatched as [::was-set timeout-id]
 :timeout [::timed-out]}
```

Similarly, an interval:

```cljs
{:ms 3000
 :set [::was-set] ;; dispatched as [::was-set interval-id]
 :interval [::period-reached]}
```

These are given as arguments to events or effects, as follows:

```cljs
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
