import { Link } from 'react-router-dom'

export default function GradientButton({
  children,
  to,
  href,
  variant = 'solid',
  className = '',
  icon: Icon,
  ...props
}) {
  const classes = `${variant === 'solid' ? 'btn-gradient' : 'btn-outline'} ${className}`

  const content = (
    <>
      {children}
      {Icon && <Icon size={18} className="shrink-0" />}
    </>
  )

  if (to) {
    return (
      <Link to={to} className={classes} {...props}>
        {content}
      </Link>
    )
  }

  if (href) {
    return (
      <a href={href} className={classes} {...props}>
        {content}
      </a>
    )
  }

  return (
    <button className={classes} {...props}>
      {content}
    </button>
  )
}
