import React from 'react';
import { Chip as HeroUIChip, ChipProps as HeroUIChipProps } from '@heroui/react';
import { tv } from 'tailwind-variants';

const chipVariants = tv({
  base: [
    "inline-flex",
    "items-center",
    "justify-center",
    "text-xs",
    "font-medium",
    "transition-all",
    "duration-200",
    "backdrop-blur-sm",
  ],
  variants: {
    variant: {
      solid: "bg-opacity-100",
      bordered: "border-2 bg-transparent",
      light: "bg-opacity-20",
      flat: "bg-opacity-15 backdrop-blur-sm",
      faded: "border border-opacity-20 bg-opacity-5",
      shadow: "shadow-lg",
      dot: "relative",
    },
    color: {
      default: "",
      primary: "",
      secondary: "",
      success: "",
      warning: "",
      danger: "",
    },
    size: {
      sm: "h-5 px-2 text-xs",
      md: "h-6 px-3 text-sm",
      lg: "h-8 px-4 text-base",
    },
    radius: {
      none: "rounded-none",
      sm: "rounded-sm",
      md: "rounded-md",
      lg: "rounded-lg",
      full: "rounded-full",
    },
  },
  compoundVariants: [
    // Primary color variants
    {
      color: "primary",
      variant: "solid",
      class: "bg-primary text-primary-foreground hover:bg-primary/90",
    },
    {
      color: "primary",
      variant: "bordered",
      class: "border-primary text-primary hover:bg-primary/10",
    },
    {
      color: "primary",
      variant: "light",
      class: "bg-primary/20 text-primary hover:bg-primary/30",
    },
    {
      color: "primary",
      variant: "flat",
      class: "bg-primary/15 text-primary hover:bg-primary/25",
    },
    {
      color: "primary",
      variant: "faded",
      class: "border-primary/20 bg-primary/5 text-primary hover:bg-primary/10",
    },
    {
      color: "primary",
      variant: "shadow",
      class: "bg-primary text-primary-foreground shadow-primary/25 hover:shadow-primary/40",
    },
    
    // Secondary color variants
    {
      color: "secondary",
      variant: "solid",
      class: "bg-secondary text-secondary-foreground hover:bg-secondary/90",
    },
    {
      color: "secondary",
      variant: "bordered",
      class: "border-secondary text-secondary hover:bg-secondary/10",
    },
    {
      color: "secondary",
      variant: "light",
      class: "bg-secondary/20 text-secondary hover:bg-secondary/30",
    },
    {
      color: "secondary",
      variant: "flat",
      class: "bg-secondary/15 text-secondary hover:bg-secondary/25",
    },
    {
      color: "secondary",
      variant: "faded",
      class: "border-secondary/20 bg-secondary/5 text-secondary hover:bg-secondary/10",
    },
    {
      color: "secondary",
      variant: "shadow",
      class: "bg-secondary text-secondary-foreground shadow-secondary/25 hover:shadow-secondary/40",
    },
    
    // Success color variants
    {
      color: "success",
      variant: "solid",
      class: "bg-success text-success-foreground hover:bg-success/90",
    },
    {
      color: "success",
      variant: "bordered",
      class: "border-success text-success hover:bg-success/10",
    },
    {
      color: "success",
      variant: "light",
      class: "bg-success/20 text-success hover:bg-success/30",
    },
    {
      color: "success",
      variant: "flat",
      class: "bg-success/15 text-success hover:bg-success/25",
    },
    {
      color: "success",
      variant: "faded",
      class: "border-success/20 bg-success/5 text-success hover:bg-success/10",
    },
    {
      color: "success",
      variant: "shadow",
      class: "bg-success text-success-foreground shadow-success/25 hover:shadow-success/40",
    },
    
    // Warning color variants
    {
      color: "warning",
      variant: "solid",
      class: "bg-warning text-warning-foreground hover:bg-warning/90",
    },
    {
      color: "warning",
      variant: "bordered",
      class: "border-warning text-warning hover:bg-warning/10",
    },
    {
      color: "warning",
      variant: "light",
      class: "bg-warning/20 text-warning hover:bg-warning/30",
    },
    {
      color: "warning",
      variant: "flat",
      class: "bg-warning/15 text-warning hover:bg-warning/25",
    },
    {
      color: "warning",
      variant: "faded",
      class: "border-warning/20 bg-warning/5 text-warning hover:bg-warning/10",
    },
    {
      color: "warning",
      variant: "shadow",
      class: "bg-warning text-warning-foreground shadow-warning/25 hover:shadow-warning/40",
    },
    
    // Danger color variants
    {
      color: "danger",
      variant: "solid",
      class: "bg-danger text-danger-foreground hover:bg-danger/90",
    },
    {
      color: "danger",
      variant: "bordered",
      class: "border-danger text-danger hover:bg-danger/10",
    },
    {
      color: "danger",
      variant: "light",
      class: "bg-danger/20 text-danger hover:bg-danger/30",
    },
    {
      color: "danger",
      variant: "flat",
      class: "bg-danger/15 text-danger hover:bg-danger/25",
    },
    {
      color: "danger",
      variant: "faded",
      class: "border-danger/20 bg-danger/5 text-danger hover:bg-danger/10",
    },
    {
      color: "danger",
      variant: "shadow",
      class: "bg-danger text-danger-foreground shadow-danger/25 hover:shadow-danger/40",
    },
    
    // Default color variants
    {
      color: "default",
      variant: "solid",
      class: "bg-foreground text-background hover:bg-foreground/90",
    },
    {
      color: "default",
      variant: "bordered",
      class: "border-foreground text-foreground hover:bg-foreground/10",
    },
    {
      color: "default",
      variant: "light",
      class: "bg-foreground/20 text-foreground hover:bg-foreground/30",
    },
    {
      color: "default",
      variant: "flat",
      class: "bg-foreground/15 text-foreground hover:bg-foreground/25",
    },
    {
      color: "default",
      variant: "faded",
      class: "border-divider bg-background text-foreground hover:bg-foreground/5",
    },
    {
      color: "default",
      variant: "shadow",
      class: "bg-foreground text-background shadow-foreground/25 hover:shadow-foreground/40",
    },
  ],
  defaultVariants: {
    variant: "solid",
    color: "default",
    size: "md",
    radius: "md",
  },
});

export interface ChipProps extends Omit<HeroUIChipProps, 'variant' | 'color' | 'size' | 'radius'> {
  variant?: 'solid' | 'bordered' | 'light' | 'flat' | 'faded' | 'shadow' | 'dot';
  color?: 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  radius?: 'none' | 'sm' | 'md' | 'lg' | 'full';
  startContent?: React.ReactNode;
  endContent?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}

const Chip: React.FC<ChipProps> = ({
  variant = 'solid',
  color = 'default',
  size = 'md',
  radius = 'md',
  startContent,
  endContent,
  children,
  className,
  ...props
}) => {
  return (
    <HeroUIChip
      variant={variant as any}
      color={color as any}
      size={size as any}
      radius={radius as any}
      startContent={startContent}
      endContent={endContent}
      className={chipVariants({ variant, color, size, radius, className })}
      {...props}
    >
      {children}
    </HeroUIChip>
  );
};

export default Chip; 