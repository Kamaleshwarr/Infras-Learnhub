import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import { Breadcrumbs, Link, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

export interface KnowledgeBreadcrumbItem {
  label: string
  href?: string
}

interface KnowledgeBreadcrumbsProps {
  items: KnowledgeBreadcrumbItem[]
}

export function KnowledgeBreadcrumbs({ items }: KnowledgeBreadcrumbsProps) {
  return (
    <Breadcrumbs aria-label="Knowledge base breadcrumb" separator={<ChevronRightIcon fontSize="small" />}>
      {items.map((item, index) => {
        const isLast = index === items.length - 1
        if (isLast || !item.href) {
          return (
            <Typography color="text.primary" key={`${item.label}-${index}`} variant="body2">
              {item.label}
            </Typography>
          )
        }
        return (
          <Link component={RouterLink} key={item.href} to={item.href} underline="hover" variant="body2">
            {item.label}
          </Link>
        )
      })}
    </Breadcrumbs>
  )
}
