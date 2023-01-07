(ns chera.num-converter)

;; number conversion based on data/code from several sources
;; currently only integer from 0 to 10e20 - 1, haven't made sure every exception yet
;; -> main source for now: https://www.tofugu.com/japanese/counting-in-japanese/
;; -> for cross checking some logic: https://github.com/Greatdane/Convert-Numbers-to-Japanese/blob/master/Convert-Numbers-to-Japanese.py

;; maths!

(defn remove-last-digit [num]
  (long (Math/floor (/ num 10))))

(defn order10 [num]
  (-> num Math/log10 Math/floor int))

(defn is-last-digit? [inp last-dig]
  (let [last-dig-order (order10 last-dig)
        last-dig-to-compare (int (rem inp (Math/pow 10 (inc last-dig-order))))]
    (= last-dig last-dig-to-compare)))

;; handling 0..10000

(def digit->kango
  {0 "ゼロ" 1 "いち" 2 "に" 3 "さん" 4 "よん" 5 "ご" 6 "ろく" 7 "なな" 8 "はち" 9 "きゅう"})

(def special-1digit-u10000
  {[1 1]  "じゅう"
   [1 2]  "ひゃく"
   [3 2]  "さんびゃく"
   [6 2]  "ろっぴゃく"
   [8 2]  "はっぴゃく"
   [1 3]  "いっせん"
   [3 3]  "さんぜん"
   [8 3]  "はっせん"})

(defn digit-u10000->yomi
  "convert digit u(nder) 10000 to japanese reading"
  [digit order]
  (or (special-1digit-u10000 [digit order])
      (when-not (= digit 0)
        (let [kango (digit->kango digit)]
          (cond (= 3 order) (str kango "せん")
                (= 2 order) (str kango "ひゃく")
                (= 1 order) (str kango "じゅう")
                :else kango)))))

(defn westarabic-u10000->yomi [number]
  (->> (loop [num number order 0
              acc []]
         (let [digit (mod num 10)
               yomi (digit-u10000->yomi digit order)]
           (if (< num 10)
             (conj acc yomi)
             (recur (remove-last-digit num) (inc order)
                    (conj acc yomi)))))
       (reduce str)))

;; handling every log10eN, data struct {:log10e4 a :num b}, b is 0..10000

(defn special-ld-yomi
  "special l(ast) d(igit) on certain l(og10)e4 order"
  [log10e4 num]
  (let [handle (fn [tail] (fn [ld] {:num (- num ld)  :tail tail}))]
    (condp (fn [[le4' ld] [le4 num]]
             (when (and (= le4 le4') (is-last-digit? num ld)) ld))
           [log10e4 num]
      [3  1] :>> (handle "いっちょう")
      [3  8] :>> (handle "はっちょう")
      [3 10] :>> (handle "じゅっちょう")
      [4  1] :>> (handle "いっきょう")
      [4  6] :>> (handle "ろっきょう")
      [4  8] :>> (handle "はっきょう")
      [4 10] :>> (handle "じゅっきょう")
      nil)))

(def log10e4->kango
  {4 "きょう"
   3 "ちょう"
   2 "おく"
   1 "まん"})

(defn handle-log10e4-yomi [{:keys [log10e4 num] :as data}]
  (or (when (= num 0) data)
      (some-> (special-ld-yomi log10e4 num)
              (as-> special-case
                    (merge data special-case)))
      (assoc data :tail
             (log10e4->kango log10e4))))

(defn break-log10e4 [inp]
  (loop [num inp
         acc []]
    (if (< (/ num 10000) 1)
      (conj acc {:log10e4 (count acc) :num num})
      (let [first-order (rem num 10000)
            remaining-order (/ (- num first-order) 10000)
            accumulated (conj acc {:log10e4 (count acc) :num first-order})]
        (recur remaining-order accumulated)))))

(defn westarab->japanese [num]
  (or (when (= num "0") "ゼロ")
      (->> num long break-log10e4
           (map handle-log10e4-yomi)
           (map #(update % :num westarabic-u10000->yomi))
           (map #(str (:num %) (:tail %)))
           (reduce str))))
