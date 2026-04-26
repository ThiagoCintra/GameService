#!/bin/bash
set -e

echo "Initializing LocalStack SQS resources..."

awslocal sqs create-queue \
    --queue-name transactions-dlq \
    --region us-east-1

DLQ_ARN=$(awslocal sqs get-queue-attributes \
    --queue-url http://localhost:4566/000000000000/transactions-dlq \
    --attribute-names QueueArn \
    --region us-east-1 \
    --query 'Attributes.QueueArn' --output text)

awslocal sqs create-queue \
    --queue-name transactions \
    --region us-east-1 \
    --attributes "RedrivePolicy={\"deadLetterTargetArn\":\"${DLQ_ARN}\",\"maxReceiveCount\":5}"

echo "SQS queues created successfully:"
awslocal sqs list-queues --region us-east-1
echo "LocalStack initialization complete"
