import { chromium } from 'playwright'
import { mkdir, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const outputDir = path.resolve(__dirname, '../../docs/screenshots/f17-featured-section')

const sampleCards = [
  {
    name: 'Spring Boot',
    shortName: 'Spring Boot',
    description: 'Opinionated framework for building production-ready JVM applications.',
    category: 'Backend',
    difficulty: 'Intermediate',
  },
  {
    name: 'React',
    shortName: 'React',
    description: 'Component-based library for building interactive user interfaces.',
    category: 'Frontend',
    difficulty: 'Intermediate',
  },
  {
    name: 'AWS',
    shortName: 'AWS',
    description: 'Cloud platform for scalable infrastructure, storage, and managed services.',
    category: 'Cloud',
    difficulty: 'Advanced',
  },
]

function beforeCard(card) {
  return `
    <div style="flex:1 1 calc(33.333% - 16px); min-width:260px; border:1px solid #e0e0e0; border-radius:4px;">
      <a href="#" style="display:block; padding:16px; text-decoration:none; color:inherit;">
        <div style="font-size:1.1rem; font-weight:500; margin-bottom:4px;">${card.name}</div>
        <div style="font-size:0.875rem; color:#666; margin-bottom:8px;">${card.shortName}</div>
        <div style="display:flex; gap:8px;">
          <span style="border:1px solid #ccc; border-radius:16px; padding:2px 8px; font-size:0.75rem;">${card.category}</span>
          <span style="border:1px solid #90caf9; border-radius:16px; padding:2px 8px; font-size:0.75rem; color:#0288d1;">${card.difficulty}</span>
        </div>
      </a>
    </div>
  `
}

function afterCard(card) {
  return `
    <div class="card" style="height:100%; border:1px solid #e0e0e0; border-radius:4px; display:flex; flex-direction:column; background:#fff; transition:box-shadow .2s,border-color .2s;">
      <div style="padding:20px; display:flex; flex-direction:column; gap:12px; flex:1;">
        <div>
          <div style="font-size:1.15rem; font-weight:700; line-height:1.25;">${card.name}</div>
          <div style="font-size:0.75rem; color:#666; margin-top:2px;">${card.shortName}</div>
        </div>
        <div style="display:flex; gap:8px; flex-wrap:wrap;">
          <span style="border:1px solid #ccc; border-radius:16px; padding:2px 8px; font-size:0.75rem;">${card.category}</span>
          <span style="border:1px solid #90caf9; border-radius:16px; padding:2px 8px; font-size:0.75rem; color:#0288d1;">${card.difficulty}</span>
        </div>
        <div style="font-size:0.875rem; color:#666; line-height:1.375; min-height:2.75rem; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
          ${card.description}
        </div>
        <div style="display:flex; gap:8px; margin-top:auto; padding-top:4px;">
          <a href="#" style="flex:1; text-align:center; background:#1976d2; color:#fff; border-radius:4px; padding:6px 12px; font-size:0.8125rem; text-decoration:none;">View Roadmap</a>
          <a href="#" style="flex:1; text-align:center; border:1px solid #1976d2; color:#1976d2; border-radius:4px; padding:6px 12px; font-size:0.8125rem; text-decoration:none;">View Details</a>
        </div>
      </div>
    </div>
  `
}

function pageShell(title, cardsHtml, variant) {
  const hoverStyle =
    variant === 'after'
      ? `<style>
          .card:hover { box-shadow: 0 4px 12px rgba(0,0,0,.12); border-color: #64b5f6 !important; cursor: pointer; }
          .grid { display:grid; grid-template-columns:repeat(3,1fr); gap:20px; align-items:stretch; }
        </style>`
      : `<style>
          .row { display:flex; flex-wrap:wrap; gap:16px; }
        </style>`

  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>${title}</title>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" />
  ${hoverStyle}
  <style>
    body { font-family: Roboto, sans-serif; margin: 0; background: #fafafa; color: #212121; }
    .wrap { max-width: 1100px; margin: 24px auto; padding: 0 16px; }
    h2 { font-size: 1.1rem; font-weight: 600; margin: 0 0 16px; }
    .hero { border:1px solid #e0e0e0; border-radius:4px; padding:20px; margin-bottom:24px; background:#fff; }
  </style>
</head>
<body>
  <div class="wrap">
    <div class="hero">
      <div style="font-size:1.35rem; margin-bottom:8px;">What do you want to learn next?</div>
      <div style="color:#666; margin-bottom:16px;">Browse technologies, search the catalog, and open a technology to see its roadmap.</div>
      <div style="border:1px solid #ccc; border-radius:4px; padding:12px; color:#999;">Search technologies...</div>
    </div>
    <h2>Featured technologies</h2>
    <div class="${variant === 'after' ? 'grid' : 'row'}">
      ${cardsHtml}
    </div>
  </div>
</body>
</html>`
}

async function main() {
  await mkdir(outputDir, { recursive: true })

  const beforeHtml = pageShell('Before', sampleCards.map(beforeCard).join(''), 'before')
  const afterHtml = pageShell('After', sampleCards.map(afterCard).join(''), 'after')

  const beforePath = path.join(outputDir, 'before.html')
  const afterPath = path.join(outputDir, 'after.html')
  await writeFile(beforePath, beforeHtml)
  await writeFile(afterPath, afterHtml)

  const browser = await chromium.launch()
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })

  await page.goto(`file://${beforePath}`)
  await page.screenshot({ path: path.join(outputDir, 'featured-section-before.png'), fullPage: true })

  await page.goto(`file://${afterPath}`)
  await page.screenshot({ path: path.join(outputDir, 'featured-section-after.png'), fullPage: true })

  const card = page.locator('.card').first()
  await card.hover()
  await page.screenshot({ path: path.join(outputDir, 'featured-section-after-hover.png'), fullPage: true })

  await browser.close()
  console.log(`Screenshots saved to ${outputDir}`)
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
