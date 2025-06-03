import { Card as NextUICard, CardBody, CardHeader, CardFooter } from '@nextui-org/react';
import type { CardProps as NextUICardProps } from '@nextui-org/react';
import React from 'react';

export interface CardProps extends NextUICardProps {
  children: React.ReactNode;
  header?: React.ReactNode;
  footer?: React.ReactNode;
}

export const Card: React.FC<CardProps> = ({ 
  children, 
  header,
  footer,
  className = '',
  ...props 
}) => {
  return (
    <NextUICard 
      className={`bg-content1/50 backdrop-blur-md border border-divider ${className}`}
      {...props}
    >
      {header && <CardHeader>{header}</CardHeader>}
      <CardBody>{children}</CardBody>
      {footer && <CardFooter>{footer}</CardFooter>}
    </NextUICard>
  );
};

export default Card; 
