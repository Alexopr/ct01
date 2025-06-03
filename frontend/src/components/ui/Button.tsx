import { Button as NextUIButton } from '@nextui-org/react';
import type { ButtonProps as NextUIButtonProps } from '@nextui-org/react';
import React from 'react';

export interface ButtonProps extends NextUIButtonProps {
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({ 
  children, 
  className = '',
  variant = 'solid',
  color = 'primary',
  ...props 
}) => {
  return (
    <NextUIButton
      variant={variant}
      color={color}
      className={`font-semibold ${className}`}
      {...props}
    >
      {children}
    </NextUIButton>
  );
};

export default Button; 
