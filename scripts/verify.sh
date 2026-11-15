#!/usr/bin/env bash
# End-to-End Verification Script for Library Management API
# Usage: ./scripts/verify.sh <BASE_URL>

set -e

BASE_URL="${1:-http://localhost:8080}"
echo "=================================================="
echo "Running End-to-End API Verification against: $BASE_URL"
echo "=================================================="

# 1. Health Check
echo -n "[1/7] Testing GET /actuator/health... "
HEALTH_RESP=$(curl -s "$BASE_URL/actuator/health")
echo "$HEALTH_RESP" | grep -q "UP" && echo "✅ UP" || (echo "❌ Failed" && exit 1)

# 2. Get Books List
echo -n "[2/7] Testing GET /books... "
BOOKS_RESP=$(curl -s "$BASE_URL/books")
echo "$BOOKS_RESP" | grep -q "Clean Code" && echo "✅ Books returned" || (echo "❌ Failed" && exit 1)

# 3. Create Book
echo -n "[3/7] Testing POST /books (Create)... "
RANDOM_ISBN="9780$(date +%s)"
CREATE_RESP=$(curl -s -X POST "$BASE_URL/books" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"DevOps Handbook Demo\",\"author\":\"Gene Kim\",\"isbn\":\"$RANDOM_ISBN\",\"category\":\"DevOps\",\"totalCopies\":2,\"publishedYear\":2021}")
BOOK_ID=$(echo "$CREATE_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✅ Created Book ID: $BOOK_ID"

# 4. Borrow Book
echo -n "[4/7] Testing POST /borrowings (Borrow)... "
BORROW_RESP=$(curl -s -X POST "$BASE_URL/borrowings" \
  -H "Content-Type: application/json" \
  -d "{\"bookId\":$BOOK_ID,\"studentName\":\"Student Test\",\"studentEmail\":\"student@test.edu\"}")
BORROW_ID=$(echo "$BORROW_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✅ Borrowed with Record ID: $BORROW_ID"

# 5. Verify Decrement
echo -n "[5/7] Verifying Available Copies Decreased... "
COPIES=$(curl -s "$BASE_URL/books/$BOOK_ID" | grep -o '"availableCopies":[0-9]*' | cut -d':' -f2)
if [ "$COPIES" -eq 1 ]; then
  echo "✅ Available copies = 1 (Expected)"
else
  echo "❌ Expected 1, got $COPIES" && exit 1
fi

# 6. Return Book
echo -n "[6/7] Testing PUT /borrowings/$BORROW_ID/return (Return)... "
RETURN_RESP=$(curl -s -X PUT "$BASE_URL/borrowings/$BORROW_ID/return")
echo "$RETURN_RESP" | grep -q "RETURNED" && echo "✅ Returned successfully" || (echo "❌ Failed" && exit 1)

# 7. Delete Book
echo -n "[7/7] Testing DELETE /books/$BOOK_ID (Delete)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/books/$BOOK_ID")
if [ "$HTTP_CODE" -eq 204 ]; then
  echo "✅ Deleted (204 No Content)"
else
  echo "❌ Failed with status $HTTP_CODE" && exit 1
fi

echo "=================================================="
echo "🎉 ALL END-TO-END VERIFICATION CHECKS PASSED!"
echo "=================================================="
