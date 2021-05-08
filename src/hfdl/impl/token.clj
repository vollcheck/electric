(ns hfdl.impl.token
  (:require [hfdl.impl.util :as u])
  (:import (java.util.concurrent.atomic AtomicReference)))

(defn token
  ([] (token u/nop))
  ([c] (AtomicReference. c)))

(defn swap-token! [^AtomicReference t c]
  (loop []
    (if-some [x (.get t)]
      (if (.compareAndSet t x c)
        (do) (recur)) (c))))

(defn burn-token! [^AtomicReference t]
  (loop []
    (when-some [c (.get t)]
      (if (.compareAndSet t c nil)
        (c) (recur)))))
