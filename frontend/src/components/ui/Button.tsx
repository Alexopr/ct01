import React from 'react';
import { Button as HeroUIButton, ButtonProps as HeroUIButtonProps } from '@heroui/react';
import { Icon } from '@iconify/react';

export interface ButtonProps extends Omit<HeroUIButtonProps, 'variant'> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'success' | 'warning';
  size?: 'sm' | 'md' | 'lg';
  icon?: string;
  iconPosition?: 'left' | 'right';
  fullWidth?: boolean;
  gradient?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  icon,
  iconPosition = 'left',
  fullWidth = false,
  gradient = false,
  children,
  className,
  ...props
}) => {
  // Determine HeroUI variant and color based on our custom variant
  const getHeroUIProps = () => {
    switch (variant) {
      case 'primary':
        return {
          color: 'primary' as const,
          variant: gradient ? 'solid' : 'solid' as const,
        };
      case 'secondary':
        return {
          color: 'secondary' as const,
          variant: 'solid' as const,
        };
      case 'ghost':
        return {
          color: 'primary' as const,
          variant: 'ghost' as const,
        };
      case 'danger':
        return {
          color: 'danger' as const,
          variant: 'solid' as const,
        };
      case 'success':
        return {
          color: 'success' as const,
          variant: 'solid' as const,
        };
      case 'warning':
        return {
          color: 'warning' as const,
          variant: 'solid' as const,
        };
      default:
        return {
          color: 'primary' as const,
          variant: 'solid' as const,
        };
    }
  };

  const heroUIProps = getHeroUIProps();

  // Custom gradient classes for primary variant
  const gradientClass = gradient && variant === 'primary' 
    ? 'bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700' 
    : '';

  const combinedClassName = [
    fullWidth ? 'w-full' : '',
    gradient && variant === 'primary' ? gradientClass : '',
    'transition-all duration-200',
    className
  ].filter(Boolean).join(' ');

  const renderIcon = (position: 'left' | 'right') => {
    if (!icon || iconPosition !== position) return null;
    return <Icon icon={icon} className="w-4 h-4" />;
  };

  return (
    <HeroUIButton
      {...heroUIProps}
      size={size}
      className={combinedClassName}
      {...props}
    >
      <div className="flex items-center gap-2">
        {renderIcon('left')}
        {children}
        {renderIcon('right')}
      </div>
    </HeroUIButton>
  );
};

export default Button; 