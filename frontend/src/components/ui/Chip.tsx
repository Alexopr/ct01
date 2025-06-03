import React from 'react';
import { Chip as HeroUIChip } from '@nextui-org/react';
import type { ChipProps as HeroUIChipProps } from '@nextui-org/react';
import { tv } from 'tailwind-variants';
import { getChipClasses } from '../../utils/colorUtils';
import type { ColorVariant } from '../../utils/colorUtils';

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
  defaultVariants: {
    variant: "solid",
    size: "md",
    radius: "md",
  },
});

export interface ChipProps extends Omit<HeroUIChipProps, 'variant' | 'color' | 'size' | 'radius'> {
  variant?: 'solid' | 'bordered' | 'light' | 'flat' | 'faded' | 'shadow' | 'dot';
  color?: ColorVariant | 'default';
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
  // Generate color classes using utility
  const getColorClassName = () => {
    if (color === 'default') {
      // Default color handling
      const defaultClasses = {
        solid: 'bg-default text-default-foreground hover:bg-default/90',
        bordered: 'border-default text-default hover:bg-default/10',
        light: 'bg-default/20 text-default hover:bg-default/30',
        flat: 'bg-default/15 text-default hover:bg-default/25',
        faded: 'border-default/20 bg-default/5 text-default hover:bg-default/10',
        shadow: 'bg-default text-default-foreground shadow-default/25 hover:shadow-default/40',
        dot: 'bg-default',
      };
      return defaultClasses[variant || 'solid'];
    }

    // Use centralized color utility for other colors
    if (variant && variant !== 'dot') {
      return getChipClasses(color as ColorVariant, variant);
    }
    
    return '';
  };

  const baseClasses = chipVariants({ 
    variant: variant === 'dot' ? 'solid' : variant, 
    size, 
    radius 
  });
  
  const colorClasses = getColorClassName();
  const combinedClassName = `${baseClasses} ${colorClasses} ${className || ''}`.trim();

  return (
    <HeroUIChip
      {...props}
      className={combinedClassName}
    >
      {startContent && (
        <span className="mr-1">
          {startContent}
        </span>
      )}
      
      {variant === 'dot' && (
        <span className={`
          absolute -top-1 -right-1 w-2 h-2 rounded-full 
          ${colorClasses || 'bg-primary'}
        `} />
      )}
      
      <span className="truncate">
        {children}
      </span>
      
      {endContent && (
        <span className="ml-1">
          {endContent}
        </span>
      )}
    </HeroUIChip>
  );
};

export default Chip; 
