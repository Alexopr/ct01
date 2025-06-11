import React, { useEffect, useState } from 'react';
import { 
  Card, 
  CardBody, 
  CardHeader, 
  Button, 
  Chip, 
  Divider,
  Spinner
} from '@nextui-org/react';
import { Icon } from '@iconify/react';
import { subscriptionApi } from '../../services/api';
import type { SubscriptionPlan } from '../../types/api';

interface SubscriptionPlansProps {
  onPlanSelect?: (plan: SubscriptionPlan) => void;
  currentPlan?: string;
}

export const SubscriptionPlans: React.FC<SubscriptionPlansProps> = ({ 
  onPlanSelect,
  currentPlan 
}) => {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const plansResponse = await subscriptionApi.getPlans();
        setPlans(plansResponse);
      } catch (err) {
        setError('Ошибка загрузки тарифных планов');
        console.error('Error fetching subscription plans:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const formatPrice = (price: number, currency: string, billingCycle: string) => {
    if (price === 0) return 'Бесплатно';
    const period = billingCycle === 'MONTHLY' ? 'мес' : 'год';
    return `${price} ${currency}/${period}`;
  };



  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center p-8">
        <p className="text-danger">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Plans Comparison Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {plans.map((plan) => {
          const isCurrentPlan = currentPlan === plan.id;
          const isRecommended = plan.isRecommended;
          
          return (
            <Card 
              key={plan.id} 
              className={`relative ${isCurrentPlan ? 'ring-2 ring-primary' : ''} ${isRecommended ? 'ring-2 ring-warning' : ''}`}
            >
              {isRecommended && (
                <div className="absolute -top-3 left-1/2 transform -translate-x-1/2">
                  <Chip color="warning" size="sm">
                    Рекомендуется
                  </Chip>
                </div>
              )}
              
              {isCurrentPlan && (
                <div className="absolute -top-3 right-4">
                  <Chip color="primary" size="sm">
                    Текущий план
                  </Chip>
                </div>
              )}

              <CardHeader className="flex flex-col items-start gap-2 pb-2">
                <h3 className="text-xl font-bold">{plan.displayName}</h3>
                <p className="text-default-600 text-sm">{plan.description}</p>
                <div className="text-2xl font-bold text-primary">
                  {formatPrice(plan.price, plan.currency, plan.billingCycle)}
                </div>
                {plan.trialDays && plan.trialDays > 0 && (
                  <Chip size="sm" color="success" variant="flat">
                    {plan.trialDays} дней бесплатно
                  </Chip>
                )}
              </CardHeader>

              <Divider />

              <CardBody className="space-y-4">
                <div className="space-y-2">
                  <h4 className="font-semibold text-sm">Возможности:</h4>
                  <ul className="space-y-1">
                    {plan.features.map((feature, index) => (
                      <li key={index} className="flex items-center gap-2 text-sm">
                        <Icon icon="material-symbols:check" className="h-4 w-4 text-success flex-shrink-0" />
                        <span>{feature}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <Button
                  color={isCurrentPlan ? "default" : "primary"}
                  variant={isCurrentPlan ? "bordered" : "solid"}
                  onPress={() => !isCurrentPlan && onPlanSelect?.(plan)}
                  disabled={isCurrentPlan}
                  className="w-full"
                >
                  {isCurrentPlan ? 'Активный план' : 'Выбрать план'}
                </Button>
              </CardBody>
            </Card>
          );
        })}
      </div>


    </div>
  );
}; 