; *Inspired by [David A Wheeler's Curly Infix syntax](http://goo.gl/zARpL).*
; 
; Descriptions are hard.
; 
;     #[a + b + c + d] -> (+ a b c d)
;     #[a + #[b / c]] -> (+ a (/ b c))
;     #[a < b < c = d > e] -> (and (< a b c) (= c d) (> d e))

(ns abuse.infix
    (:use abuse.core))

(def boolean-and-ables (ref #{'< '<= '= '== 'not= '>= '>}))

; TODO: more concise, as promised.
(defn infix-with-and
  [forms]
    (list* 'and (loop
      [previous-operand (first forms) remaining (rest forms) result []]
      (if (not (seq remaining))
        result
        (recur (nth remaining 1) (drop 2 remaining) (conj result
          (list (first remaining) previous-operand (nth remaining 1))))))))

(defmacro infixed
  [& forms]
    (let [vforms (vec (for [form forms]
                           (if (vector? form) `(infixed ~@form) form)))]
      (if (even? (count vforms))
        (throw (Exception. "Even number of forms in infix expression.")))
      (if (= 0 (count forms))
        (throw (Exception. "Infix expression needs forms.")))
      (if (= 1 (count forms))
        (first forms)
        (if (apply = (take-nth 2 (rest vforms))) ; 
          (list* (nth forms 1) (take-nth 2 vforms))
          (if (every? @boolean-and-ables (take-nth 2 (rest vforms)))
            (list* (infix-with-and vforms))
            (throw (Exception. "Invalid distinct infix operators.")))))))

(def infixed-open "#[")
(def infixed-close \])

(defn read-infixed
  [reader initial-char]
    `(infixed ~@(read-delimited-list nil infixed-close reader true)))

(set-reader-macro infixed-open read-infixed)
