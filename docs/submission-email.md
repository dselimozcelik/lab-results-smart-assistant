# Gönderim E-postası Taslağı

**Konu:** Teknik Değerlendirme Teslimi - Lab Results Smart Assistant

Merhaba,

Lab Results Smart Assistant teknik değerlendirme çalışmamı tamamladım.

Repo bağlantısı:
[https://github.com/dselimozcelik/lab-results-smart-assistant](https://github.com/dselimozcelik/lab-results-smart-assistant)

Çözüm; laboratuvar cihazını simüle eden ayrı bir mock servis, cihazı periyodik olarak poll eden ve
sonuçları doğrulayıp PostgreSQL'e kaydeden Spring Boot backend, JWT ile korunan React doktor arayüzü
ve yerel Ollama üzerinden kontrollü AI ön analizinden oluşmaktadır.

Tüm sistemi çalıştırmak için:

```bash
ollama pull gemma2:9b
docker compose -f docker-compose.full.yml up --build
```

Uygulama adresi: `http://localhost:5173`

```text
Kullanıcı adı: doctor
Şifre: Doctor123!
```

Değerlendirme için önerdiğim doküman sırası:

1. [`README.md`](../README.md) - hızlı değerlendirme, mimari ve öne çıkan kararlar
2. [`docs/requirements-traceability.md`](requirements-traceability.md) - case maddelerinin karşılanma matrisi
3. [`docs/technical-design.md`](technical-design.md) - teknik kararlar, trade-off'lar ve production alternatifleri
4. [`docs/testing-and-evidence.md`](testing-and-evidence.md) - test stratejisi ve failure-mode kanıtları
5. [`docs/demo-guide.md`](demo-guide.md) - tekrarlanabilir kurulum ve demo akışı

Çözümde normal akışın yanında duplicate, eksik alan, geçersiz birim, stale veri, cihaz kesintisi,
timeout ve bozuk LLM çıktısı gibi senaryoları da ele aldım. LLM'e anormallik kararı verdirmedim;
durumlar backend tarafından deterministic olarak hesaplanmakta, model yalnızca verilen paneli
yorumlamaktadır.

AI agent araçlarından geliştirme hızını artırmak amacıyla yararlandım. Mimari yön, scope, UX kararları,
kabul kriterleri ve doğrulama sorumluluğu bana aittir. Ayrıntılı açıklama
[`docs/ai-assisted-development.md`](ai-assisted-development.md) belgesinde yer almaktadır.

Refresh token rotation, ek roller, WebSocket, multi-model LLM, multi-instance scheduler locking ve
Kubernetes case kapsamı dışında bırakılmıştır. Bu kararların gerekçeleri ve production alternatifleri
dokümanlarda açıkça belirtilmiştir.

İyi çalışmalar.

