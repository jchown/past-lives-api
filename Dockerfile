FROM public.ecr.aws/lambda/java:17

COPY dead-people.zip ${LAMBDA_TASK_ROOT}
COPY target/classes ${LAMBDA_TASK_ROOT}
COPY target/dependency/* ${LAMBDA_TASK_ROOT}/lib/

CMD [ "com.datasmelter.pastlives.Handler" ]