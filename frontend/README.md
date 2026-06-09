# Frontend — Doctor UI

Lab Results Smart Assistant'ın React + TypeScript + Vite arayüzü. Doktor giriş yapar;
hastalarını, tüplerini ve test panellerini görür, anormal sonuçları renk + rozetle ayırt
eder ve bir tüp için LLM ön analizi ister.

Sunucu durumu (cache, retry, `keepPreviousData`) TanStack Query ile yönetilir; JWT token
yalnızca sayfa yaşam döngüsü boyunca memory'de tutulur, kalıcı storage'a yazılmaz.

Kurulum, demo akışı ve mimari kararlar repo kökündeki belgelerde:
[README](../README.md) · [Kurulum ve Demo](../docs/kurulum-ve-demo.md) ·
[Teknik Tasarım](../docs/teknik-tasarim.md).

## Komutlar

```bash
npm ci          # bağımlılıklar
npm run dev     # geliştirme sunucusu (Vite, http://localhost:5173)
npm test        # Vitest + Testing Library (davranış testleri)
npm run lint    # ESLint
npm run build   # tsc tip kontrolü + production build (dist/)
npm run preview # build çıktısını lokal önizleme
```

## Konfigürasyon

API base URL `VITE_API_BASE_URL` build argümanıyla ayarlanır (`src/api/client.ts`).
Boş bırakılırsa istekler same-origin gider; full Docker kurulumunda nginx `/api` isteklerini
backend'e proxy eder, böylece CORS yüküne gerek kalmaz.
