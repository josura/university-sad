#include <iostream>
#include <cstdio>
#include <cstdlib>
#include "Tree.cpp"
using namespace std;

template<class T>
class Splaytree:public Tree<T>{
    Node<T>* RR_Rotate(Node<T>* k2)
   {
      Node<T>* k1 = k2->left;
      k2->left = k1->right;
      k1->right = k2;
      return k1;
   }
   Node<T>* LL_Rotate(Node<T>* k2)
   {
      Node<T>* k1 = k2->right;
      k2->right = k1->left;
      k1->left = k2;
      return k1;
   }
   Node<T>* Splay(int key, Node<T>* root)
   {
      if (!root)
      return NULL;
      s header;
      header.left= header.right = NULL;
      Node<T>* LeftTreeMax = &header;
      Node<T>* RightTreeMin = &header;
      while (1)
      {
         if (key < root->k)
         {
            if (!root->left)
            break;
            if (key< root->left->k)
            {
               root = RR_Rotate(root);
               if (!root->left)
               break;
            }
            RightTreeMin->left= root;
            RightTreeMin = RightTreeMin->left;
            root = root->left;
            RightTreeMin->left = NULL;
         }
         else if (key> root->k)
         {
            if (!root->right)
            break;
            if (key > root->right->k)
            {
               root = LL_Rotate(root);
               if (!root->right)
               break;
            }
            LeftTreeMax->right= root;
            LeftTreeMax = LeftTreeMax->right;
            root = root->right;
            LeftTreeMax->right = NULL;
         }
         else
         break;
      }
      LeftTreeMax->right = root->left;
      RightTreeMin->left = root->right;
      root->left = header.right;
      root->right = header.left;
      return root;
   }

    public:
        void Insert(T element) final{

        }
        Node<T>* Searight(int element) final{
            return NULL;
        }
        bool Delete(T element) final{
            return true;
        }


};