import React from 'react';
import { Input as HeroUIInput, InputProps as HeroUIInputProps } from '@heroui/react';
import { Icon } from '@iconify/react';

export interface InputProps extends Omit<HeroUIInputProps, 'variant'> {
  variant?: 'default' | 'bordered' | 'flat' | 'faded' | 'underlined';
  state?: 'default' | 'error' | 'success' | 'warning';
  leftIcon?: string;
  rightIcon?: string;
  onRightIconClick?: () => void;
  fullWidth?: boolean;
  glassmorphism?: boolean;
}

export const Input: React.FC<InputProps> = ({
  variant = 'default',
  state = 'default',
  leftIcon,
  rightIcon,
  onRightIconClick,
  fullWidth = false,
  glassmorphism = false,
  className,
  ...props
}) => {
  const getHeroUIVariant = () => {
    switch (variant) {
      case 'bordered':
        return 'bordered';
      case 'flat':
        return 'flat';
      case 'faded':
        return 'faded';
      case 'underlined':
        return 'underlined';
      default:
        return 'flat';
    }
  };

  const getColorFromState = () => {
    switch (state) {
      case 'error':
        return 'danger';
      case 'success':
        return 'success';
      case 'warning':
        return 'warning';
      default:
        return 'default';
    }
  };

  const getGlassmorphismClasses = () => {
    if (!glassmorphism) return '';
    return 'backdrop-blur-lg bg-background/60 border border-divider/20';
  };

  const combinedClassName = [
    fullWidth ? 'w-full' : '',
    glassmorphism ? getGlassmorphismClasses() : '',
    'transition-all duration-200',
    className
  ].filter(Boolean).join(' ');

  const startContent = leftIcon ? (
    <Icon icon={leftIcon} className="w-4 h-4 text-foreground-500" />
  ) : undefined;

  const endContent = rightIcon ? (
    <div
      className={`flex items-center ${onRightIconClick ? 'cursor-pointer hover:text-foreground-700' : ''}`}
      onClick={onRightIconClick}
    >
      <Icon icon={rightIcon} className="w-4 h-4 text-foreground-500" />
    </div>
  ) : undefined;

  return (
    <HeroUIInput
      variant={getHeroUIVariant()}
      color={getColorFromState()}
      startContent={startContent}
      endContent={endContent}
      className={combinedClassName}
      classNames={{
        input: 'text-foreground',
        inputWrapper: glassmorphism ? 'backdrop-blur-lg bg-background/60 border border-divider/20' : '',
      }}
      {...props}
    />
  );
};

export default Input; 