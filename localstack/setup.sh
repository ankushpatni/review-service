#!/bin/bash

# Create the S3 bucket
echo "Creating S3 bucket..."
awslocal s3 mb s3://review-data-bucket

# Create a sample review file
echo "Creating sample review file..."
cat > sample-review.jl << 'EOL'
{"hotelId": 10984, "platform": "Agoda", "hotelName": "Oscar Saigon Hotel", "comment": {"isShowReviewResponse": false, "hotelReviewId": 948353737, "providerId": 332, "rating": 6.4, "checkInDateMonthAndYear": "April 2025", "encryptedReviewData": "cZwJ6a6ZoFX2W5WwVXaJkA==", "formattedRating": "6.4", "formattedReviewDate": "April 10, 2025", "ratingText": "Good", "responderName": "Oscar Saigon Hotel", "responseDateText": "", "responseTranslateSource": "en", "reviewComments": "Hotel room is basic and very small. not much like pictures. few areas were getting repaired. but since location is so accessible from all main areas in district-1, i would prefer to stay here again. Staff was good.", "reviewNegatives": "", "reviewPositives": "", "reviewProviderLogo": "", "reviewProviderText": "Agoda", "reviewTitle": "Perfect location and safe but hotel under renovation ", "translateSource": "en", "translateTarget": "en", "reviewDate": "2025-04-10T05:37:00+07:00", "reviewerInfo": {"countryName": "India", "displayMemberName": "********", "flagName": "in", "reviewGroupName": "Solo traveler", "roomTypeName": "Premium Deluxe Double Room", "countryId": 35, "lengthOfStay": 2, "reviewGroupId": 3, "roomTypeId": 0, "reviewerReviewedCount": 0, "isExpertReviewer": false, "isShowGlobalIcon": false, "isShowReviewedCount": false}, "originalTitle": "", "originalComment": "", "formattedResponseDate": ""}, "overallByProviders": [{"providerId": 332, "provider": "Agoda", "overallScore": 7.9, "reviewCount": 7070, "grades": {"Cleanliness": 7.7, "Facilities": 7.2, "Location": 9.1, "Room comfort and quality": 7.5, "Service": 7.8, "Value for money": 7.8}}]}
{"hotelId": 10985, "platform": "Agoda", "hotelName": "Grand Hotel", "comment": {"isShowReviewResponse": true, "hotelReviewId": 948353738, "providerId": 332, "rating": 8.2, "checkInDateMonthAndYear": "May 2025", "encryptedReviewData": "aZwJ6a6ZoFX2W5WwVXaJkA==", "formattedRating": "8.2", "formattedReviewDate": "May 15, 2025", "ratingText": "Very Good", "responderName": "Grand Hotel", "responseDateText": "Thank you for your review!", "responseTranslateSource": "en", "reviewComments": "Great hotel with excellent service. Room was clean and comfortable. Great location near the city center.", "reviewNegatives": "", "reviewPositives": "Great location, friendly staff", "reviewProviderLogo": "", "reviewProviderText": "Agoda", "reviewTitle": "Excellent stay", "translateSource": "en", "translateTarget": "en", "reviewDate": "2025-05-15T08:30:00+07:00", "reviewerInfo": {"countryName": "United States", "displayMemberName": "John", "flagName": "us", "reviewGroupName": "Couple", "roomTypeName": "Deluxe Room", "countryId": 1, "lengthOfStay": 3, "reviewGroupId": 2, "roomTypeId": 1, "reviewerReviewedCount": 5, "isExpertReviewer": true, "isShowGlobalIcon": true, "isShowReviewedCount": true}, "originalTitle": "", "originalComment": "", "formattedResponseDate": "May 17, 2025"}, "overallByProviders": [{"providerId": 332, "provider": "Agoda", "overallScore": 8.5, "reviewCount": 5230, "grades": {"Cleanliness": 8.7, "Facilities": 8.2, "Location": 9.3, "Room comfort and quality": 8.5, "Service": 8.8, "Value for money": 7.9}}]}
EOL

# Upload the sample file to S3
echo "Uploading sample file to S3..."
awslocal s3 cp sample-review.jl s3://review-data-bucket/reviews/sample-review-$(date +%Y%m%d).jl

# List bucket contents
echo "Listing S3 bucket contents:"
awslocal s3 ls s3://review-data-bucket/reviews/ --recursive

echo "Setup completed successfully!" 