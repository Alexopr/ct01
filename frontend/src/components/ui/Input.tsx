import { Input as NextUIInput } from '@nextui-org/react';
import type { InputProps as NextUIInputProps } from '@nextui-org/react';
import React from 'react';

export interface InputProps extends NextUIInputProps {
  // Add any custom props here if needed
}

export const Input: React.FC<InputProps> = ({ 
  className = '',
  variant = 'bordered',
  ...props 
}) => {
  return (
    <NextUIInput
      variant={variant}
      className={`${className}`}
      {...props}
    />
  );
};

export default Input; 
