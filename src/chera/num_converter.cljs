(ns chera.num-converter)

;; number conversion based on data/code from several sources
;; currently only integer from 0 to 10e4 - 1, haven't made sure every exception yet
;; -> main source for now: https://www.tofugu.com/japanese/counting-in-japanese/
;; -> for cross checking some logic: https://github.com/Greatdane/Convert-Numbers-to-Japanese/blob/master/Convert-Numbers-to-Japanese.py

; naming could use some work, basically juu -> digit 0-9 (zero is nil), e -> 10eN , scientific notation symbol
(def juu-map {\1 "いち"
              \2 "に"
              \3 "さん"
              \4 "よん"
              \5 "ご"
              \6 "ろく"
              \7 "なな"
              \8 "はち"
              \9 "きゅう"})

(def e-map {0 #(juu-map %)
            1 #(case %
                 \0 nil
                 \1 "じゅう"
                 (str (juu-map %) "じゅう"))
            2 #(case %
                 \0 nil
                 \1 "ひゃく"
                 \3 "さんびゃく"
                 \6 "ろっぴゃく"
                 \8 "はっぴゃく"
                 (str (juu-map %) "ひゃく"))
            3 #(case %
                 \0 nil
                 \1 "せん"
                 \3 "さんぜん"
                 \8 "はっせん"
                 (str (juu-map %) "せん"))
            4 #(str (juu-map %) "まん")})

;; the name of numeral system refers to this https://en.wikipedia.org/wiki/List_of_numeral_systems
(defn west-arabic->japanese [num]
  (as-> (str num) it
    (reverse it) (map-indexed vector it)
    (map (fn [[e juu]] (apply (get e-map e (constantly "?")) [juu])) it)
    (reduce #(str %2 %1) it)
    (if (nil? it) "ゼロ" it))) ;; TODO better exceptions handling later
