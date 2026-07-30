import { chromium } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const outputDir = path.resolve(__dirname, '../../docs/screenshots/phase5-ui-polish')

const metrics = [
  { title: 'Active Initiatives', value: '12', helper: 'Currently active learning programs' },
  { title: 'Pending Reviews', value: '5', helper: 'Certificate submissions awaiting review' },
]

function beforeMetricCard(metric) {
  return `
    <div style="border:1px solid #e0e0e0;border-radius:12px;padding:20px;background:#fff;transition:box-shadow .2s,border-color .2s;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
        <span style="color:#666;font-size:14px;">${metric.title}</span>
        <span style="color:#1f5eff;">★</span>
      </div>
      <div style="font-size:2rem;font-weight:700;margin-bottom:8px;">${metric.value}</div>
      <div style="color:#666;font-size:14px;">${metric.helper}</div>
    </div>
  `
}

function afterMetricCard(metric) {
  return `
    <div class="metric" style="border:1px solid #e8ecf1;border-radius:12px;padding:20px;background:#fff;transition:box-shadow .2s,background .2s;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
        <span style="color:#5f6b7a;font-size:14px;font-weight:500;">${metric.title}</span>
        <span class="metric-icon" style="color:#5f6b7a;">★</span>
      </div>
      <div style="font-size:2rem;font-weight:700;margin-bottom:8px;letter-spacing:-0.02em;">${metric.value}</div>
      <div style="color:#5f6b7a;font-size:14px;">${metric.helper}</div>
    </div>
    <style>
      .metric:hover {
        box-shadow: 0 4px 12px rgba(17,24,39,0.08);
        background: linear-gradient(145deg, #ffffff 0%, rgba(31,94,255,0.045) 100%);
      }
      .metric:hover .metric-icon {
        color: #1f5eff;
        filter: drop-shadow(0 0 8px rgba(31,94,255,0.35));
      }
    </style>
  `
}

function pageShell(title, cardsHtml) {
  return `
    <!DOCTYPE html>
    <html lang="en">
      <head>
        <meta charset="UTF-8" />
        <title>${title}</title>
        <style>
          body { font-family: Inter, Roboto, Arial, sans-serif; background: #f6f8fb; padding: 32px; }
          h1 { font-size: 1.25rem; margin-bottom: 16px; }
          .grid { display: grid; gap: 16px; grid-template-columns: repeat(2, minmax(0, 1fr)); max-width: 900px; }
        </style>
      </head>
      <body>
        <h1>${title}</h1>
        <div class="grid">${cardsHtml}</div>
      </body>
    </html>
  `
}

async function capture(page, html, filename) {
  await page.setContent(html, { waitUntil: 'domcontentloaded' })
  await page.screenshot({ path: path.join(outputDir, filename), fullPage: true })
}

await mkdir(outputDir, { recursive: true })
const browser = await chromium.launch()
const page = await browser.newPage({ viewport: { width: 1280, height: 720 } })

await capture(
  page,
  pageShell('Before — metric card hover (blue border)', metrics.map(beforeMetricCard).join('')),
  'dashboard-metrics-before.png',
)

await capture(
  page,
  pageShell('After — metric card hover (gradient + glow)', metrics.map(afterMetricCard).join('')),
  'dashboard-metrics-after.png',
)

const welcomeBanner = `
  <!DOCTYPE html>
  <html lang="en">
    <head>
      <meta charset="UTF-8" />
      <style>
        body { font-family: Inter, Roboto, Arial, sans-serif; background: #f6f8fb; padding: 32px; }
        .banner {
          max-width: 960px;
          padding: 24px 28px;
          border-radius: 12px;
          border: 1px solid #e8ecf1;
          background: linear-gradient(135deg, #ffffff 0%, rgba(31,94,255,0.06) 100%);
        }
        .greeting { color: #5f6b7a; font-size: 14px; font-weight: 500; margin-bottom: 4px; }
        h1 { font-size: 2rem; font-weight: 700; letter-spacing: -0.02em; margin: 0 0 8px; }
        p { color: #5f6b7a; line-height: 1.6; max-width: 720px; margin: 0; }
      </style>
    </head>
    <body>
      <div class="banner">
        <div class="greeting">Welcome back, Jane Doe</div>
        <h1>Employee Dashboard</h1>
        <p>Your learning activity and resources.</p>
      </div>
    </body>
  </html>
`

await capture(page, welcomeBanner, 'dashboard-welcome-banner-after.png')

await browser.close()
console.log(`Screenshots saved to ${outputDir}`)
