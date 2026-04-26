#!/usr/bin/env bash
# Creates SQS queues in LocalStack: transactions and transactions-dlq
set -euo pipefail
ENDPOINT=${AWS_ENDPOINT:-http://localhost:4566}
ACCOUNT_ID=${AWS_ACCOUNT_ID:-000000000000}
QUEUE_NAME=${QUEUE_NAME:-transactions}
DLQ_NAME=${DLQ_NAME:-transactions-dlq}

echo "Creating DLQ $DLQ_NAME on $ENDPOINT"
aws --endpoint-url="$ENDPOINT" sqs create-queue --queue-name "$DLQ_NAME"
DLQ_URL=$(aws --endpoint-url="$ENDPOINT" sqs get-queue-url --queue-name "$DLQ_NAME" --output text)

echo "Creating queue $QUEUE_NAME with RedrivePolicy to DLQ"
aws --endpoint-url="$ENDPOINT" sqs create-queue --queue-name "$QUEUE_NAME" --attributes "RedrivePolicy={\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:$ACCOUNT_ID:$DLQ_NAME\",\"maxReceiveCount\":5}"

echo "Done. Queues created:"
aws --endpoint-url="$ENDPOINT" sqs list-queues
