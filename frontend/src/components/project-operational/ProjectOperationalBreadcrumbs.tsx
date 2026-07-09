import { Breadcrumbs, Link, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

export interface OperationalBreadcrumbItem {
  label: string
  href?: string
}

interface ProjectOperationalBreadcrumbsProps {
  items: OperationalBreadcrumbItem[]
}

export function ProjectOperationalBreadcrumbs({ items }: ProjectOperationalBreadcrumbsProps) {
  return (
    <Breadcrumbs aria-label="Project navigation" sx={{ flexWrap: 'wrap' }}>
      {items.map((item, index) => {
        const isLast = index === items.length - 1
        if (!item.href || isLast) {
          return (
            <Typography color="text.primary" key={`${item.label}-${index}`} variant="body2">
              {item.label}
            </Typography>
          )
        }
        return (
          <Link component={RouterLink} key={`${item.label}-${index}`} to={item.href} underline="hover" variant="body2">
            {item.label}
          </Link>
        )
      })}
    </Breadcrumbs>
  )
}

export function buildProjectOperationalBreadcrumbs(
  projectId: string,
  projectName: string,
  sectionLabel: string,
  sectionHref: string,
): OperationalBreadcrumbItem[] {
  return [
    { label: projectName, href: `/projects/${projectId}` },
    { label: sectionLabel, href: sectionHref },
  ]
}
