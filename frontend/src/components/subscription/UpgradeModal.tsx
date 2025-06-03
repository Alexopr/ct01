import React, { useState } from 'react';
import {
  Modal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Button,
  Card,
  CardBody,
  CardHeader,
  Chip,
  Divider,
  RadioGroup,
  Radio,
  Input,
  Spinner
} from '@nextui-org/react';
import { Icon } from '@iconify/react';
import { subscriptionApi } from '../../services/api';
import type { SubscriptionPlan, SubscriptionUpgradeRequest } from '../../types/api';

interface UpgradeModalProps {
  isOpen: boolean;
  onClose: () => void;
  plans: SubscriptionPlan[];
  currentPlan?: string;
  onUpgradeSuccess?: () => void;
}

export const UpgradeModal: React.FC<UpgradeModalProps> = ({
  isOpen,
  onClose,
  plans,
  currentPlan,
  onUpgradeSuccess
}) => {
  const [selectedPlan, setSelectedPlan] = useState<string>('');
  const [billingCycle, setBillingCycle] = useState<'MONTHLY' | 'YEARLY'>('MONTHLY');
  const [promoCode, setPromoCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClose = () => {
    setSelectedPlan('');
    setBillingCycle('MONTHLY');
    setPromoCode('');
    setError(null);
    onClose();
  };

  const handleUpgrade = async () => {
    if (!selectedPlan) {
      setError('Выберите тарифный план');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      const request: SubscriptionUpgradeRequest = {
        planId: selectedPlan,
        billingCycle,
        autoRenewal: true,
        promoCode: promoCode || undefined
      };

      await subscriptionApi.upgrade(request);
      onUpgradeSuccess?.();
      handleClose();
    } catch (err) {
      setError('Ошибка при обновлении подписки');
      console.error('Error upgrading subscription:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price: number, currency: string, cycle: 'MONTHLY' | 'YEARLY') => {
    if (price === 0) return 'Бесплатно';
    const period = cycle === 'MONTHLY' ? 'мес' : 'год';
    return `${price} ${currency}/${period}`;
  };

  const getDiscountedPrice = (plan: SubscriptionPlan, cycle: 'MONTHLY' | 'YEARLY') => {
    if (cycle === 'YEARLY' && plan.billingCycle === 'MONTHLY') {
      const yearlyPrice = plan.price * 10; // 2 months free
      return { price: yearlyPrice, discount: 2 };
    }
    return { price: plan.price, discount: 0 };
  };

  const availablePlans = plans.filter(plan => plan.id !== currentPlan);

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={handleClose}
      size="3xl"
      scrollBehavior="inside"
      classNames={{
        backdrop: "bg-gradient-to-t from-zinc-900 to-zinc-900/10 backdrop-opacity-20"
      }}
    >
      <ModalContent>
        <ModalHeader className="flex flex-col gap-1">
          <h2 className="text-xl font-bold">Обновление подписки</h2>
          <p className="text-sm text-default-600">
            Выберите тарифный план и период оплаты
          </p>
        </ModalHeader>
        
        <ModalBody className="gap-6">
          {/* Billing Cycle Selection */}
          <div className="space-y-3">
            <h3 className="text-lg font-semibold">Период оплаты</h3>
            <RadioGroup
              value={billingCycle}
              onValueChange={(value) => setBillingCycle(value as 'MONTHLY' | 'YEARLY')}
              orientation="horizontal"
            >
              <Radio value="MONTHLY">Ежемесячно</Radio>
              <Radio value="YEARLY">
                <div className="flex items-center gap-2">
                  Ежегодно
                  <Chip size="sm" color="success" variant="flat">
                    2 месяца в подарок
                  </Chip>
                </div>
              </Radio>
            </RadioGroup>
          </div>

          {/* Plan Selection */}
          <div className="space-y-3">
            <h3 className="text-lg font-semibold">Выберите план</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {availablePlans.map((plan) => {
                const { price, discount } = getDiscountedPrice(plan, billingCycle);
                const isSelected = selectedPlan === plan.id;
                
                return (
                  <Card
                    key={plan.id}
                    isPressable
                    isHoverable
                    onPress={() => setSelectedPlan(plan.id)}
                    className={`cursor-pointer transition-colors ${
                      isSelected ? 'ring-2 ring-primary bg-primary/5' : ''
                    }`}
                  >
                    <CardHeader className="flex justify-between items-start">
                      <div>
                        <h4 className="font-bold">{plan.displayName}</h4>
                        <p className="text-sm text-default-600">{plan.description}</p>
                      </div>
                      {isSelected && (
                        <Icon icon="material-symbols:check-circle" className="h-6 w-6 text-primary" />
                      )}
                    </CardHeader>
                    
                    <CardBody className="pt-0">
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <span className="text-xl font-bold text-primary">
                            {formatPrice(price, plan.currency, billingCycle)}
                          </span>
                          {discount > 0 && (
                            <Chip size="sm" color="success" variant="flat">
                              -{discount} мес.
                            </Chip>
                          )}
                        </div>
                        
                        <div className="space-y-1">
                          {plan.features.slice(0, 3).map((feature, index) => (
                            <div key={index} className="flex items-center gap-2 text-sm">
                              <Icon icon="material-symbols:check" className="h-4 w-4 text-success" />
                              <span>{feature}</span>
                            </div>
                          ))}
                          {plan.features.length > 3 && (
                            <p className="text-sm text-default-500">
                              +{plan.features.length - 3} дополнительных возможностей
                            </p>
                          )}
                        </div>
                      </div>
                    </CardBody>
                  </Card>
                );
              })}
            </div>
          </div>

          {/* Promo Code */}
          <div className="space-y-3">
            <h3 className="text-lg font-semibold">Промокод (опционально)</h3>
            <Input
              placeholder="Введите промокод"
              value={promoCode}
              onValueChange={setPromoCode}
              startContent={<Icon icon="material-symbols:local-offer" />}
            />
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-danger-50 border border-danger-200 rounded-lg p-3">
              <p className="text-danger-800 text-sm">{error}</p>
            </div>
          )}
        </ModalBody>
        
        <ModalFooter>
          <Button variant="light" onPress={handleClose}>
            Отмена
          </Button>
          <Button
            color="primary"
            onPress={handleUpgrade}
            isLoading={loading}
            isDisabled={!selectedPlan || loading}
          >
            {loading ? 'Обработка...' : 'Обновить подписку'}
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  );
}; 