(ns wip.counter
  (:require [hyperfiddle.photon :as p]
            [hyperfiddle.photon-dom :as dom]
            [hyperfiddle.photon-ui :as ui])
 (:import (hyperfiddle.photon Pending)))

;; https://eugenkiss.github.io/7guis/tasks#counter

(p/defn Counter []
  (let [!state (atom 0)]
    (dom/div
     (dom/p (dom/text (p/watch !state)))
     (ui/button {:on-click (p/fn [_] (swap! !state inc) nil)}
                (dom/text "Count")))))

(def main
  #?(:cljs (p/client
             (p/main
               (try
                 (binding [dom/node (dom/by-id "root")]
                   (Counter.))
                 (catch Pending _))))))

(comment
  #?(:clj (user/browser-main! `main))
  )