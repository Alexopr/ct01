/**
 * Centralized color utility functions and constants
 * Eliminates code duplication across components
 */

export type ColorVariant = 'primary' | 'secondary' | 'success' | 'warning' | 'danger';

/**
 * Color configuration interface
 */
export interface ColorClasses {
  iconBg: string;
  iconColor: string;
  gradientBorder?: string;
  hoverBg?: string;
}

/**
 * Comprehensive color variants configuration
 */
export const COLOR_VARIANTS: Record<ColorVariant, ColorClasses> = {
  primary: {
    iconBg: 'from-primary/20 to-primary/30',
    iconColor: 'text-primary',
    gradientBorder: 'hover:shadow-primary/20',
    hoverBg: 'hover:bg-primary/10'
  },
  secondary: {
    iconBg: 'from-secondary/20 to-secondary/30',
    iconColor: 'text-secondary',
    gradientBorder: 'hover:shadow-secondary/20',
    hoverBg: 'hover:bg-secondary/10'
  },
  success: {
    iconBg: 'from-success/20 to-success/30',
    iconColor: 'text-success',
    gradientBorder: 'hover:shadow-success/20',
    hoverBg: 'hover:bg-success/10'
  },
  warning: {
    iconBg: 'from-warning/20 to-warning/30',
    iconColor: 'text-warning',
    gradientBorder: 'hover:shadow-warning/20',
    hoverBg: 'hover:bg-warning/10'
  },
  danger: {
    iconBg: 'from-danger/20 to-danger/30',
    iconColor: 'text-danger',
    gradientBorder: 'hover:shadow-danger/20',
    hoverBg: 'hover:bg-danger/10'
  }
} as const;

/**
 * Get color classes for a specific color variant
 * @param color - The color variant to get classes for
 * @returns ColorClasses object with all the styling classes
 */
export const getColorClasses = (color: ColorVariant): ColorClasses => {
  return COLOR_VARIANTS[color] || COLOR_VARIANTS.primary;
};

/**
 * Check if a color variant is valid
 * @param color - The color string to validate
 * @returns boolean indicating if the color is a valid variant
 */
export const isValidColorVariant = (color: string): color is ColorVariant => {
  return Object.keys(COLOR_VARIANTS).includes(color);
};

/**
 * Get all available color variants
 * @returns Array of all color variant keys
 */
export const getAvailableColors = (): ColorVariant[] => {
  return Object.keys(COLOR_VARIANTS) as ColorVariant[];
};

/**
 * Chip variant color mappings for tailwind-variants
 */
export const CHIP_COLOR_MAPPINGS = {
  primary: {
    solid: 'bg-primary text-primary-foreground hover:bg-primary/90',
    bordered: 'border-primary text-primary hover:bg-primary/10',
    light: 'bg-primary/20 text-primary hover:bg-primary/30',
    flat: 'bg-primary/15 text-primary hover:bg-primary/25',
    faded: 'border-primary/20 bg-primary/5 text-primary hover:bg-primary/10',
    shadow: 'bg-primary text-primary-foreground shadow-primary/25 hover:shadow-primary/40'
  },
  secondary: {
    solid: 'bg-secondary text-secondary-foreground hover:bg-secondary/90',
    bordered: 'border-secondary text-secondary hover:bg-secondary/10',
    light: 'bg-secondary/20 text-secondary hover:bg-secondary/30',
    flat: 'bg-secondary/15 text-secondary hover:bg-secondary/25',
    faded: 'border-secondary/20 bg-secondary/5 text-secondary hover:bg-secondary/10',
    shadow: 'bg-secondary text-secondary-foreground shadow-secondary/25 hover:shadow-secondary/40'
  },
  success: {
    solid: 'bg-success text-success-foreground hover:bg-success/90',
    bordered: 'border-success text-success hover:bg-success/10',
    light: 'bg-success/20 text-success hover:bg-success/30',
    flat: 'bg-success/15 text-success hover:bg-success/25',
    faded: 'border-success/20 bg-success/5 text-success hover:bg-success/10',
    shadow: 'bg-success text-success-foreground shadow-success/25 hover:shadow-success/40'
  },
  warning: {
    solid: 'bg-warning text-warning-foreground hover:bg-warning/90',
    bordered: 'border-warning text-warning hover:bg-warning/10',
    light: 'bg-warning/20 text-warning hover:bg-warning/30',
    flat: 'bg-warning/15 text-warning hover:bg-warning/25',
    faded: 'border-warning/20 bg-warning/5 text-warning hover:bg-warning/10',
    shadow: 'bg-warning text-warning-foreground shadow-warning/25 hover:shadow-warning/40'
  },
  danger: {
    solid: 'bg-danger text-danger-foreground hover:bg-danger/90',
    bordered: 'border-danger text-danger hover:bg-danger/10',
    light: 'bg-danger/20 text-danger hover:bg-danger/30',
    flat: 'bg-danger/15 text-danger hover:bg-danger/25',
    faded: 'border-danger/20 bg-danger/5 text-danger hover:bg-danger/10',
    shadow: 'bg-danger text-danger-foreground shadow-danger/25 hover:shadow-danger/40'
  }
} as const;

/**
 * Generate chip classes for a specific color and variant combination
 * @param color - The color variant
 * @param variant - The chip variant style
 * @returns CSS class string for the chip
 */
export const getChipClasses = (
  color: ColorVariant, 
  variant: keyof typeof CHIP_COLOR_MAPPINGS.primary
): string => {
  return CHIP_COLOR_MAPPINGS[color]?.[variant] || CHIP_COLOR_MAPPINGS.primary[variant];
}; 
