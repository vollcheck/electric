(ns wip.demo-explorer
  #?(:cljs (:require-macros wip.demo-explorer))
  (:require [clojure.datafy :refer [datafy]]
            [clojure.core.protocols :refer [nav]]
            #?(:clj clojure.java.io)
            [contrib.datafy-fs #?(:clj :as :cljs :as-alias) fs]
            contrib.str
            [hyperfiddle.api :as hf]
            [hyperfiddle.photon :as p]
            [hyperfiddle.photon-dom2 :as dom]
            [hyperfiddle.router :as router]
            [wip.explorer :as explorer :refer [Explorer]]
            [wip.gridsheet :as-alias gridsheet]))

(def unicode-folder "\uD83D\uDCC2") ; 📂

;(p/defn File [x]
;  (binding
;    [explorer/cols [::fs/name ::fs/modified ::fs/size ::fs/kind]
;     explorer/Search? (p/fn [m s] (includes-str? (::fs/name m) s))]
;    (let [m (datafy x)
;          xs [m]]
;      (Explorer. (::fs/absolute-path m) xs
;                 {::dom/style {:height "calc((20 + 1) * 24px)"}
;                  ::explorer/page-size 20
;                  ::explorer/row-height 24
;                  ::gridsheet/grid-template-columns "auto 8em 5em 3em"}))))

(p/defn Dir [x]
  (binding
    [explorer/cols [::fs/name ::fs/modified ::fs/size ::fs/kind]]
    (let [m (datafy x)
          xs (nav m ::fs/children (::fs/children m))]
      (p/client (dom/h1 (dom/text (p/server (::fs/absolute-path m)))))
      (Explorer.
        (explorer/tree-lister xs ::fs/children #(contrib.str/includes-str? (::fs/name %) %2))
        {::dom/style {:height "calc((20 + 1) * 24px)"}
         ::explorer/page-size 20
         ::explorer/row-height 24
         ::gridsheet/grid-template-columns "auto 8em 5em 3em"}))))

(p/defn App []
  (p/client
    (dom/link (dom/props {:rel :stylesheet, :href "user/demo-explorer.css"}))
    (dom/div (dom/props {:class "photon-demo-explorer"})
      (binding [router/build-route (fn [[self s & route] route']
                                     ; links are global, swap-route is local !!!
                                     ; root local links through this entrypoint
                                     `[App ~s ~route'])]
        (p/server
          (binding [explorer/Format
                    (p/fn [m a]
                      (let [v (a m)]
                        (case a
                          ::fs/name (case (::fs/kind m)
                                      ::fs/dir (let [absolute-path (::fs/absolute-path m)]
                                                 (p/client (router/link [::fs/dir absolute-path] (dom/text v))))
                                      (::fs/other ::fs/symlink ::fs/unknown-kind) (p/client (dom/text v))
                                      (p/client (dom/text v)) #_(p/client (router/Link. [::fs/file x] (dom/text v))))
                          ::fs/modified (p/client (some-> v .toLocaleDateString dom/text))
                          ::fs/kind (case (::fs/kind m)
                                      ::fs/dir (p/client (dom/text unicode-folder))
                                      (p/client (some-> v name dom/text)))
                          (p/client (dom/text (str v))))))]
            (let [[self s route] (p/client router/route)
                  [page fs-path] (or route [::fs/dir (fs/absolute-path "node_modules")])]
              (p/client
                (router/router 1
                  (p/server
                    (case page
                      ;::fs/file (File. (clojure.java.io/file fs-path))
                      ::fs/dir (Dir. (clojure.java.io/file fs-path)))))))))))))

; Improvements
; Native search
; lazy folding/unfolding directories (no need for pagination)
; forms (currently table hardcoded with recursive pull)
; useful ::fs/file route
