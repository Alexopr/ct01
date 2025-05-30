import React from 'react';
import { Card as HeroUICard, CardBody, CardHeader, CardFooter, CardProps as HeroUICardProps } from '@heroui/react';

export interface CardProps extends Omit<HeroUICardProps, 'variant'> {
  variant?: 'default' | 'glass' | 'gradient' | 'bordered' | 'elevated';
  size?: 'sm' | 'md' | 'lg';
  header?: React.ReactNode;
  footer?: React.ReactNode;
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hoverable?: boolean;
  clickable?: boolean;
  blur?: boolean;
}

export const Card: React.FC<CardProps> = ({
  variant = 'default',
  size = 'md',
  header,
  footer,
  padding = 'md',
  hoverable = false,
  clickable = false,
  blur = false,
  children,
  className,
  ...props
}) => {
  const getVariantClasses = () => {
    switch (variant) {
      case 'glass':
        return 'bg-background/60 backdrop-blur-lg border border-divider/20 shadow-lg';
      case 'gradient':
        return 'bg-gradient-to-br from-background to-background/80 border border-divider/30 shadow-xl';
      case 'bordered':
        return 'bg-background border-2 border-divider shadow-md';
      case 'elevated':
        return 'bg-background shadow-2xl border border-divider/50';
      default:
        return 'bg-background border border-divider shadow-sm';
    }
  };

  const getSizeClasses = () => {
    switch (size) {
      case 'sm':
        return 'max-w-sm';
      case 'lg':
        return 'max-w-2xl';
      default:
        return 'max-w-lg';
    }
  };

  const getPaddingClasses = () => {
    switch (padding) {
      case 'none':
        return 'p-0';
      case 'sm':
        return 'p-3';
      case 'lg':
        return 'p-8';
      default:
        return 'p-6';
    }
  };

  const hoverClasses = hoverable ? 'hover:shadow-xl hover:scale-[1.02] transition-all duration-300' : '';
  const clickableClasses = clickable ? 'cursor-pointer active:scale-[0.98]' : '';
  const blurClasses = blur ? 'backdrop-blur-md' : '';

  const combinedClassName = [
    getVariantClasses(),
    getSizeClasses(),
    hoverClasses,
    clickableClasses,
    blurClasses,
    'rounded-xl transition-all duration-200',
    className
  ].filter(Boolean).join(' ');

  return (
    <HeroUICard
      className={combinedClassName}
      {...props}
    >
      {header && (
        <CardHeader className="pb-0 pt-4 px-6 flex-col items-start">
          {header}
        </CardHeader>
      )}
      
      <CardBody className={getPaddingClasses()}>
        {children}
      </CardBody>
      
      {footer && (
        <CardFooter className="pt-0 pb-4 px-6">
          {footer}
        </CardFooter>
      )}
    </HeroUICard>
  );
};

export default Card; 