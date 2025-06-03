import React from 'react';
import { Icon } from '@iconify/react';

export interface AlertProps {
  type?: 'info' | 'success' | 'warning' | 'error';
  title?: string;
  description?: string;
  variant?: 'filled' | 'bordered' | 'flat' | 'glass';
  closable?: boolean;
  onClose?: () => void;
  icon?: string | boolean;
  className?: string;
  children?: React.ReactNode;
}

export const Alert: React.FC<AlertProps> = ({
  type = 'info',
  title,
  description,
  variant = 'filled',
  closable = false,
  onClose,
  icon = true,
  className,
  children,
}) => {
  const getTypeConfig = () => {
    switch (type) {
      case 'success':
        return {
          icon: 'solar:check-circle-bold',
          colorClasses: {
            filled: 'bg-success-500 text-white border-success-600',
            bordered: 'bg-success-50 text-success-900 border-success-300',
            flat: 'bg-success-100 text-success-900 border-success-200',
            glass: 'bg-success-500/20 text-success-700 border-success-300/50 backdrop-blur-lg'
          }
        };
      case 'warning':
        return {
          icon: 'solar:danger-triangle-bold',
          colorClasses: {
            filled: 'bg-warning-500 text-white border-warning-600',
            bordered: 'bg-warning-50 text-warning-900 border-warning-300',
            flat: 'bg-warning-100 text-warning-900 border-warning-200',
            glass: 'bg-warning-500/20 text-warning-700 border-warning-300/50 backdrop-blur-lg'
          }
        };
      case 'error':
        return {
          icon: 'solar:close-circle-bold',
          colorClasses: {
            filled: 'bg-danger-500 text-white border-danger-600',
            bordered: 'bg-danger-50 text-danger-900 border-danger-300',
            flat: 'bg-danger-100 text-danger-900 border-danger-200',
            glass: 'bg-danger-500/20 text-danger-700 border-danger-300/50 backdrop-blur-lg'
          }
        };
      default: // info
        return {
          icon: 'solar:info-circle-bold',
          colorClasses: {
            filled: 'bg-primary-500 text-white border-primary-600',
            bordered: 'bg-primary-50 text-primary-900 border-primary-300',
            flat: 'bg-primary-100 text-primary-900 border-primary-200',
            glass: 'bg-primary-500/20 text-primary-700 border-primary-300/50 backdrop-blur-lg'
          }
        };
    }
  };

  const config = getTypeConfig();
  const iconToShow = typeof icon === 'string' ? icon : (icon ? config.icon : null);

  const combinedClassName = [
    'rounded-lg border p-4 transition-all duration-200',
    config.colorClasses[variant],
    className
  ].filter(Boolean).join(' ');

  return (
    <div className={combinedClassName} role="alert">
      <div className="flex items-start gap-3">
        {iconToShow && (
          <Icon 
            icon={iconToShow} 
            className={`w-5 h-5 flex-shrink-0 mt-0.5 ${
              variant === 'filled' ? 'text-current' : 'text-current'
            }`} 
          />
        )}
        
        <div className="flex-1 min-w-0">
          {title && (
            <div className="font-semibold text-sm mb-1">
              {title}
            </div>
          )}
          
          {description && (
            <div className={`text-sm ${variant === 'filled' ? 'text-current opacity-90' : 'text-current opacity-80'}`}>
              {description}
            </div>
          )}
          
          {children && (
            <div className="mt-2">
              {children}
            </div>
          )}
        </div>
        
        {closable && onClose && (
          <button
            onClick={onClose}
            className={`flex-shrink-0 p-1 rounded-md transition-colors duration-200 ${
              variant === 'filled' 
                ? 'hover:bg-white/20 text-current' 
                : 'hover:bg-current/10 text-current'
            }`}
            aria-label="Закрыть уведомление"
          >
            <Icon icon="solar:close-square-bold" className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
};

export default Alert; 
