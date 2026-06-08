# AI Prompt Deney Günlüğü

Bu dosya, LLM analiz prompt'unun nasıl şekillendiğini kaydeder: hangi yaklaşımlar
denendi, ne gibi sorunlar görüldü, neden mevcut sürümde karar kılındı. Amaç, mülakatta
"prompt'u nasıl tasarladın?" sorusuna somut cevap verebilmek.

Güncel model: `gemma2:9b`. İlk deneylerde `llama3.2:3b` ve `qwen2.5:7b` de kullanıldı.
Tüm karşılaştırmalarda temperature 0 seçildi.
Önemli ilke: **Backend her testin durumunu (NORMAL/LOW/HIGH/CRITICAL/INVALID) kesin olarak
hesaplar.** Modelin işi durumu yeniden hesaplamak DEĞİL, verilen durumları düz dille
açıklamaktır. Disclaimer her zaman backend tarafından zorlanır (modelden gelmez).

---

## v1 — Tek test prompt'u (Faz 4)

**Yaklaşım:** Tek bir lab sonucu için analiz. "Use ONLY the values provided, do not invent
reference ranges."

**Sınır:** Tek test bazlıydı. Panel refactor'ında bir tüp birden çok test taşıyınca yetersiz
kaldı — doktor paneli bir bütün olarak görmek ister.

---

## v2 — Panel (tüp) prompt'u (Faz 5)

**Yaklaşım:** Tüm tüpü birlikte özetle. Her test için backend ad/değer/aralık/durum verir;
model paneli bütün olarak yorumlar. INVALID testleri "değer yok" diye etiketler.

**Gerçek çıktılarda görülen sorunlar (llama3.2:3b ile):**

1. **Yanlış sayısal karşılaştırma.** GLU 85.3 (referans 70–110, backend durumu NORMAL) için
   model "glucose level is slightly below the reference range" dedi. Model, backend'in zaten
   NORMAL dediği bir değeri yeniden (ve yanlış) yorumladı.

2. **İmkânsız değerleri sorgulamıyor.** WBC -7.9 (negatif lökosit, fiziksel olarak imkânsız)
   için "critically low WBC" dedi; "bu değer hatalı olabilir" uyarısı vermedi.

3. **flaggedTests test kodu döndürüyor**, test adı değil ("WBC" yerine "Lökosit" beklenir).
   Kullanıcı arayüzünde okunabilirlik düşük.

**Kök neden:** Prompt, modelin durumu kendi başına yorumlamasına alan bırakıyordu. Küçük model
sayısal akıl yürütmede güvenilmez.

---

## v3 — Türkçe + "açıkla, yeniden hesaplama" + güçlü model (ara sürüm)

Üç değişiklik birlikte yapıldı:

**1. Prompt yaklaşımı değişti.** Modele açıkça söylendi: her testin durumu (NORMAL/LOW/HIGH/
CRITICAL/INVALID) backend tarafından zaten hesaplandı, köşeli parantez içinde `[DURUM=...]`
olarak veriliyor ve KESİN. Model durumu yeniden hesaplamaz, sadece açıklar. Ayrıca: cevap
Türkçe, flaggedTests test adıyla, imkânsız değerler (negatif sayım) veri/cihaz hatası olarak
işaretlenir.

**2. Özet formatı netleşti.** `AnomalySummaryBuilder` her satırı `[DURUM=X] Ad: değer (referans)`
biçimine getirdi; durumu başa ve köşeli paranteze alarak modelin onu otoriter/sabit kabul
etmesini kolaylaştırdık.

**3. Model yükseltildi.** `llama3.2:3b` → `qwen2.5:7b`. 3B model Türkçe üretirken İngilizce ile
karışıyordu ("recommended followups", "usededildi"). qwen2.5:7b (M1/16GB'a rahat sığar) Türkçeyi
akıcı üretiyor.

**Görülen son sorun ve çözümü:** Model, İngilizce test adlarını Türkçe cümle içinde çevirmeye
çalışıp bozuyordu — en uç örnek "White Blood Cell" → **"beygir hücre sayımı"**. Prompt'a
"test adlarını ASLA çevirme, birebir İngilizce yaz" kuralı + doğru/yanlış örnek eklendi.

**Sonuç (qwen2.5:7b + v3, gerçek çıktılar):**
- Durumlar doğru: GLU 85.3 (NORMAL) artık yanlışlıkla "düşük" denmiyor.
- Negatif WBC (-7.9) "veri/cihaz hatası olabilir" diye not ediliyor, körü körüne "kritik düşük"
  denmiyor.
- flaggedTests her zaman birebir test adıyla geliyor ("White Blood Cell Count", "Sodium").
- "beygir/krm" gibi saçmalıklar bitti. Özet serbest metninde test adı bazen birebir İngilizce
  ("white blood cell count yüksek"), bazen düzgün Türkçe ("beyaz kan hücre sayımı düşük") —
  ikisi de kabul edilebilir; UI'da gösterilen flaggedTests her zaman doğru.

**Neden burada karar kılındı:** flaggedTests (arayüzde gösterilen kritik alan) güvenilir,
özetler akıcı ve durumlarla tutarlı, modelin durumu yeniden hesaplaması engellendi. Daha fazla
sıkılaştırma küçük modelde getirisi azalan bir uğraş; bu seviye demo için yeterli ve savunulabilir.

**Üretimde:** daha büyük/tıbbi alana özel bir model, per-test klinik panik değerleri (sabit
heuristik yerine), ve özet üretimini kuyruğa alma (senkron yerine) düşünülürdü.

---

## Model karşılaştırması: qwen2.5:7b vs gemma2:9b (M1 / 16GB)

Aynı v3 prompt'u, aynı 3 tüp, temperature 0 ile her iki modele doğrudan gönderildi.

| Kriter | qwen2.5:7b | gemma2:9b |
|---|---|---|
| Türkçe akıcılık | iyi, ama ara sıra "Pasien" gibi kaymalar | daha temiz, kaymasız |
| Durum doğruluğu (flaggedTests) | bir tüpte LOW potasyumu kaçırdı | LOW potasyumu doğru yakaladı |
| İmkânsız değer (negatif WBC) farkı | çok iyi açıkladı ("fiziksel olarak imkânsız") | fark etmedi, "kritik düşük" dedi |
| Hız (M1) | daha hızlı | biraz daha yavaş |
| RAM | ~5-6GB | ~6-7GB |

**Karar: gemma2:9b.** Arayüzde gösterilen kritik alan flaggedTests'in doğruluğu ve genel Türkçe
temizliği bizim için imkânsız-değer farkındalığından daha önemli. Negatif değer zaten backend
tarafında CRITICAL/INVALID olarak işaretlendiği için, modelin onu ayrıca "imkânsız" demesi
ikincil. gemma2:9b 16GB M1'e sığıyor; daha büyük qwen2.5:14b denenebilirdi ama 16GB'da sınırda
kalıp yavaşlardı — demo için hız/kalite dengesi gemma2:9b'de daha iyi.

Model `application-dev.yml` üzerinden değiştirilebilir; kod hiç değişmez (config-driven).

---

## v4 — "Tekrar etme, yorumla" (mevcut)

**Sorun (v3'te):** Özetler doktorun zaten tablodan gördüğü şeyi tekrar ediyordu: "WBC düşük,
diğerleri normal." Takipler de çok genel: "ek testler gerekebilir." Doktora ekstra değer katmıyordu.

**Değişiklik:** Prompt'a açıkça "değerleri TEKRAR ETME; bulguların klinik olarak ne anlama
gelebileceğini yorumla" görevi eklendi. Önemli denge: daha çok yorum isterken modeli tanı koymaya
veya ilaç önermeye itmemek. Bu yüzden:
- Yorumlar OLASILIK diliyle ("...düşündürebilir", "...ile uyumlu olabilir"), kesin tanı yok.
- Takipler somut ama REÇETESİZ (hangi yönde değerlendirme, hangi ek test, neyin izlenmesi);
  ilaç adı / doz yok.
- summary 3-4 cümle, suggestedFollowups 2-3 somut madde.
- Güvenlik korkulukları (durumu yeniden hesaplama, uydurma, INVALID/imkânsız değer, test adını
  çevirme) v3'ten aynen korundu.

**Önce / sonra (gerçek çıktı, WBC=2.6 LOW):**
- v3: "Beyaz kan hücresi sayısı düşük bulunmuştur. Diğer testler normal." Takip: "Semptomları
  ve tıbbi geçmişi sorgulayın."
- v4: "...beyaz kan hücresi sayımı düşük bulunmuştur. Bu durum bağışıklık sisteminin zayıfladığı
  veya enfeksiyonla mücadelede zorluk çektiği bir durumu düşündürebilir." Takip: "Enfeksiyon
  hikayesini değerlendirin; bağışıklığı etkileyebilecek nedenleri araştırın; gerekirse tam kan
  sayımı talep edin."

**Not:** Daha uzun/yorumlayıcı cevap istemek, küçük modelin JSON'ı bozma veya timeout'a yaklaşma
ihtimalini hafif artırır. 5/5 ardışık çağrı sorunsuz geçti; nadir bir parse/timeout hatası zaten
graceful olarak ele alınıyor (kullanıcı "AI analizi şu an kullanılamıyor" görür, sistem çökmez).
Bu yüzden ollama timeout 60s'te tutuldu.

**Karar:** v4 + gemma2:9b. Yorum derinliği ile güvenlik korkulukları arasında doğru denge bu
sürümde sağlandı.

**Güvenlik sınırı:** Prompt kuralları ve JSON doğrulaması model davranışını azaltır fakat serbest
metnin klinik doğruluğunu garanti etmez. Backend; deterministic durumları, `flaggedTests` listesini,
çıktı boyutlarını ve disclaimer'ı zorlar. Serbest özet ve takip önerileri doktor tarafından
incelenmesi gereken güvenilmeyen model çıktısıdır.
