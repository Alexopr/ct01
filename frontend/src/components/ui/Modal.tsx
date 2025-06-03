import {
  Modal as NextUIModal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
} from '@nextui-org/react';
import React from 'react';

export interface ModalProps {
  children: React.ReactNode;
  title?: React.ReactNode;
  footer?: React.ReactNode;
  isOpen?: boolean;
  onClose?: () => void;
  className?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl' | '5xl' | 'full';
  placement?: 'auto' | 'top' | 'center' | 'bottom';
}

export const Modal: React.FC<ModalProps> = ({ 
  children, 
  title,
  footer,
  className = '',
  ...props 
}) => {
  return (
    <NextUIModal 
      className={`${className}`}
      {...props}
    >
      <ModalContent>
        {title && <ModalHeader>{title}</ModalHeader>}
        <ModalBody>{children}</ModalBody>
        {footer && <ModalFooter>{footer}</ModalFooter>}
      </ModalContent>
    </NextUIModal>
  );
};

export default Modal; 
