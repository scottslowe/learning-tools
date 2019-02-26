#!/bin/bash -ex

aws rds delete-db-instance --db-instance-identifier awsinaction-db-restore --skip-final-snapshot
aws rds delete-db-instance --db-instance-identifier awsinaction-db-restore-time --skip-final-snapshot
aws rds delete-db-snapshot --db-snapshot-identifier wordpress-manual-snapshot
aws rds delete-db-snapshot --db-snapshot-identifier wordpress-copy-snapshot
aws --region eu-west-1 rds delete-db-snapshot --db-snapshot-identifier wordpress-manual-snapshot
