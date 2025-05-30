import React from 'react';
import {
  Modal as HeroUIModal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  ModalProps as HeroUIModalProps,
} from '@heroui/react';
import { Icon } from '@iconify/react';

export interface ModalProps extends Omit<HeroUIModalProps, 'size'> {
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
  title?: React.ReactNode;
  description?: string;
  footer?: React.ReactNode;
  variant?: 'default' | 'glass' | 'gradient';
  closeButton?: boolean;
  blur?: boolean;
  scrollBehavior?: 'inside' | 'outside';
}

export const Modal: React.FC<ModalProps> = ({
  size = 'md',
  title,
  description,
  footer,
  variant = 'default',
  closeButton = true,
  blur = false,
  scrollBehavior = 'inside',
  children,
  onClose,
  className,
  ...props
}) => {
  const getVariantClasses = () => {
    switch (variant) {
      case 'glass':
        return 'bg-background/80 backdrop-blur-lg border border-divider/30';
      case 'gradient':
        return 'bg-gradient-to-br from-background to-background/90 border border-divider/20';
      default:
        return 'bg-background border border-divider';
    }
  };

  const combinedClassName = [
    getVariantClasses(),
    blur ? 'backdrop-blur-md' : '',
    'shadow-2xl',
    className
  ].filter(Boolean).join(' ');

  return (
    <HeroUIModal
      size={size}
      scrollBehavior={scrollBehavior}
      onClose={onClose}
      classNames={{
        wrapper: 'z-[100]',
        backdrop: 'bg-black/50 backdrop-blur-sm',
        base: combinedClassName,
      }}
      {...props}
    >
      <ModalContent>
        {(onClose) => (
          <>
            {(title || closeButton) && (
              <ModalHeader className="flex flex-col gap-1 pr-10 relative">
                <div className="flex-1">
                  {typeof title === 'string' ? (
                    <h2 className="text-xl font-semibold text-foreground">
                      {title}
                    </h2>
                  ) : (
                    title
                  )}
                  {description && (
                    <p className="text-sm text-foreground-600 mt-1">
                      {description}
                    </p>
                  )}
                </div>
                
                {closeButton && (
                  <button
                    onClick={onClose}
                    className="absolute top-2 right-2 p-2 rounded-lg hover:bg-foreground/10 transition-colors duration-200"
                    aria-label="Закрыть модальное окно"
                  >
                    <Icon icon="solar:close-square-bold" className="w-5 h-5 text-foreground-500" />
                  </button>
                )}
              </ModalHeader>
            )}
            
            <ModalBody className="gap-4">
              {children}
            </ModalBody>
            
            {footer && (
              <ModalFooter>
                {footer}
              </ModalFooter>
            )}
          </>
        )}
      </ModalContent>
    </HeroUIModal>
  );
};

export default Modal; 