(ns chera.num-converter)

;; number conversion based on data/code from several sources
;; currently only integer from 0 to 10e20 - 1, haven't made sure every exception yet
;; -> main source for now: https://www.tofugu.com/japanese/counting-in-japanese/
;; -> for cross checking some logic: https://github.com/Greatdane/Convert-Numbers-to-Japanese/blob/master/Convert-Numbers-to-Japanese.py

(def special-2digit {'([17 \1] [16 \0]) {:yomi "じゅっきょう"}
                     '([13 \1] [12 \0]) {:yomi "じゅっちょう"}})

(def special-1digit {[1 \1]  {:yomi "じゅう"}
                     [2 \1]  {:yomi "ひゃく"}
                     [2 \3]  {:yomi "さんびゃく"}
                     [2 \6]  {:yomi "ろっぴゃく"}
                     [2 \8]  {:yomi "はっぴゃく"}
                     [3 \1]  {:yomi "せん"} ;; this needs clarification
                     [3 \3]  {:yomi "さんぜん"}
                     [3 \8]  {:yomi "はっせん"}
                     [12 \1] {:yomi "いっちょう"}
                     [12 \8] {:yomi "はっちょう"}
                     [16 \1] {:yomi "いっきょう"}
                     [16 \6] {:yomi "ろっきょう"}
                     [16 \8] {:yomi "はっきょう"}})

(def digit->kango {\0 "ゼロ"
                   \1 "いち"
                   \2 "に"
                   \3 "さん"
                   \4 "よん"
                   \5 "ご"
                   \6 "ろく"
                   \7 "なな"
                   \8 "はち"
                   \9 "きゅう"})

(defn digit->yomi [[order digit]]
  (->> (if
        (= digit \0) nil
        (let [kango (digit->kango digit)]
          (cond
            (< 16 order) (str "[?" kango "]")
            (= 16 order) (str kango "きょう")
            (= 12 order) (str kango "ちょう")
            (= 8 order)  (str kango "おく")
            (= 4 order)  (str kango "まん")
            (= 3 order) (str kango "せん")
            (= 2 order) (str kango "ひゃく")
            (= 1 order) (str kango "じゅう")
            :else kango)))
       (assoc {} :yomi)))

(defn handle-not-yomi [handler]
  (fn [elm] (if (:yomi elm) elm (or (handler elm) elm))))

(defn normalize-order [val]
  (let [[order digit] val
        normal (rem order 4)]
    (if (= normal 0) val
        [normal digit])))

(defn westarab->japanese [num]
  (cond
    (= num "0")  "ゼロ"
    :else (let [size (-> num count (- 1))]
            (->> num
                 (map-indexed (fn [idx itm] [(- size idx) itm]))
                 (partition 2 1 [:last])
                 (map (handle-not-yomi special-2digit))
                 (map (handle-not-yomi first))
                 (map (handle-not-yomi normalize-order))
                 (map (handle-not-yomi special-1digit))
                 (map (handle-not-yomi digit->yomi))
                 (map :yomi)
                 (reduce #(str %1 %2))))))
